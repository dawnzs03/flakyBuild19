/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.sms.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.message.MessageSender;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.program.EnrollmentService;
import org.hisp.dhis.program.EventService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsService;
import org.hisp.dhis.smscompression.SmsConsts.SubmissionType;
import org.hisp.dhis.smscompression.SmsResponse;
import org.hisp.dhis.smscompression.models.EnrollmentSmsSubmission;
import org.hisp.dhis.smscompression.models.SmsAttributeValue;
import org.hisp.dhis.smscompression.models.SmsEvent;
import org.hisp.dhis.smscompression.models.SmsSubmission;
import org.hisp.dhis.smscompression.models.Uid;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.trackedentity.TrackedEntityTypeService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("org.hisp.dhis.sms.listener.EnrollmentSMSListener")
@Transactional
public class EnrollmentSMSListener extends CompressionSMSListener {
  private final TrackedEntityService teService;

  private final EnrollmentService enrollmentService;

  private final TrackedEntityAttributeValueService attributeValueService;

  private final ProgramStageService programStageService;

  private final UserService userService;

  public EnrollmentSMSListener(
      IncomingSmsService incomingSmsService,
      @Qualifier("smsMessageSender") MessageSender smsSender,
      UserService userService,
      TrackedEntityTypeService trackedEntityTypeService,
      TrackedEntityAttributeService trackedEntityAttributeService,
      ProgramService programService,
      OrganisationUnitService organisationUnitService,
      CategoryService categoryService,
      DataElementService dataElementService,
      ProgramStageService programStageService,
      EventService eventService,
      TrackedEntityAttributeValueService attributeValueService,
      TrackedEntityService teService,
      EnrollmentService enrollmentService,
      IdentifiableObjectManager identifiableObjectManager) {
    super(
        incomingSmsService,
        smsSender,
        userService,
        trackedEntityTypeService,
        trackedEntityAttributeService,
        programService,
        organisationUnitService,
        categoryService,
        dataElementService,
        eventService,
        identifiableObjectManager);

    this.teService = teService;
    this.programStageService = programStageService;
    this.enrollmentService = enrollmentService;
    this.attributeValueService = attributeValueService;
    this.userService = userService;
  }

  @Override
  protected SmsResponse postProcess(IncomingSms sms, SmsSubmission submission)
      throws SMSProcessingException {
    EnrollmentSmsSubmission subm = (EnrollmentSmsSubmission) submission;

    Date enrollmentDate = subm.getEnrollmentDate();
    Date incidentDate = subm.getIncidentDate();
    Uid teUid = subm.getTrackedEntityInstance();
    Uid progid = subm.getTrackerProgram();
    Uid tetid = subm.getTrackedEntityType();
    Uid ouid = subm.getOrgUnit();
    Uid enrollmentid = subm.getEnrollment();
    OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit(ouid.getUid());

    Program program = programService.getProgram(progid.getUid());

    if (program == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_PROGRAM.set(progid));
    }

    TrackedEntityType entityType = trackedEntityTypeService.getTrackedEntityType(tetid.getUid());

    if (entityType == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_TETYPE.set(tetid));
    }

    if (!programService.hasOrgUnit(program, orgUnit)) {
      throw new SMSProcessingException(SmsResponse.OU_NOTIN_PROGRAM.set(ouid, progid));
    }

    TrackedEntity trackedEntity;
    boolean teExists = teService.trackedEntityExists(teUid.getUid());

    if (teExists) {
      log.info(String.format("Given TE [%s] exists. Updating...", teUid));
      trackedEntity = teService.getTrackedEntity(teUid.getUid());
    } else {
      log.info(String.format("Given TE [%s] does not exist. Creating...", teUid));
      trackedEntity = new TrackedEntity();
      trackedEntity.setUid(teUid.getUid());
      trackedEntity.setOrganisationUnit(orgUnit);
      trackedEntity.setTrackedEntityType(entityType);
    }

    Set<TrackedEntityAttributeValue> attributeValues = getSMSAttributeValues(subm, trackedEntity);

    if (teExists) {
      updateAttributeValues(attributeValues, trackedEntity.getTrackedEntityAttributeValues());
      trackedEntity.setTrackedEntityAttributeValues(attributeValues);
      teService.updateTrackedEntity(trackedEntity);
    } else {
      teService.createTrackedEntity(trackedEntity, attributeValues);
    }

    TrackedEntity te = teService.getTrackedEntity(teUid.getUid());

    // TODO: Unsure about this handling for enrollments, this needs to be
    // checked closely
    Enrollment enrollment;
    boolean enrollmentExists = enrollmentService.enrollmentExists(enrollmentid.getUid());
    if (enrollmentExists) {
      enrollment = enrollmentService.getEnrollment(enrollmentid.getUid());
      // Update these dates in case they've changed
      enrollment.setEnrollmentDate(enrollmentDate);
      enrollment.setIncidentDate(incidentDate);
    } else {
      enrollment =
          enrollmentService.enrollTrackedEntity(
              te, program, enrollmentDate, incidentDate, orgUnit, enrollmentid.getUid());
    }
    if (enrollment == null) {
      throw new SMSProcessingException(SmsResponse.ENROLL_FAILED.set(teUid, progid));
    }
    enrollment.setStatus(getCoreProgramStatus(subm.getEnrollmentStatus()));
    enrollment.setGeometry(convertGeoPointToGeometry(subm.getCoordinates()));
    enrollmentService.updateEnrollment(enrollment);

    // We now check if the enrollment has events to process
    User user = userService.getUser(subm.getUserId().getUid());
    List<Object> errorUIDs = new ArrayList<>();
    if (subm.getEvents() != null) {
      for (SmsEvent event : subm.getEvents()) {
        errorUIDs.addAll(processEvent(event, user, enrollment, sms));
      }
    }
    enrollment.setStatus(getCoreProgramStatus(subm.getEnrollmentStatus()));
    enrollment.setGeometry(convertGeoPointToGeometry(subm.getCoordinates()));
    enrollmentService.updateEnrollment(enrollment);

    if (!errorUIDs.isEmpty()) {
      return SmsResponse.WARN_DVERR.setList(errorUIDs);
    }

    if (attributeValues == null || attributeValues.isEmpty()) {
      // TODO: Is this correct handling?
      return SmsResponse.WARN_AVEMPTY;
    }

    return SmsResponse.SUCCESS;
  }

  private TrackedEntityAttributeValue findAttributeValue(
      TrackedEntityAttributeValue attributeValue,
      Set<TrackedEntityAttributeValue> attributeValues) {
    return attributeValues.stream()
        .filter(v -> v.getAttribute().getUid().equals(attributeValue.getAttribute().getUid()))
        .findAny()
        .orElse(null);
  }

  private void updateAttributeValues(
      Set<TrackedEntityAttributeValue> attributeValues,
      Set<TrackedEntityAttributeValue> oldAttributeValues) {
    // Update existing and add new values
    for (TrackedEntityAttributeValue attributeValue : attributeValues) {
      TrackedEntityAttributeValue oldAttributeValue =
          findAttributeValue(attributeValue, oldAttributeValues);
      if (oldAttributeValue != null) {
        oldAttributeValue.setValue(attributeValue.getValue());
        attributeValueService.updateTrackedEntityAttributeValue(oldAttributeValue);
      } else {
        attributeValueService.addTrackedEntityAttributeValue(attributeValue);
      }
    }

    // Delete any that don't exist anymore
    for (TrackedEntityAttributeValue oldAttributeValue : oldAttributeValues) {
      if (findAttributeValue(oldAttributeValue, attributeValues) == null) {
        attributeValueService.deleteTrackedEntityAttributeValue(oldAttributeValue);
      }
    }
  }

  @Override
  protected boolean handlesType(SubmissionType type) {
    return (type == SubmissionType.ENROLLMENT);
  }

  private Set<TrackedEntityAttributeValue> getSMSAttributeValues(
      EnrollmentSmsSubmission submission, TrackedEntity entityInstance) {
    if (submission.getValues() == null) {
      return null;
    }
    return submission.getValues().stream()
        .map(v -> createTrackedEntityValue(v, entityInstance))
        .collect(Collectors.toSet());
  }

  protected TrackedEntityAttributeValue createTrackedEntityValue(
      SmsAttributeValue SMSAttributeValue, TrackedEntity te) {
    Uid attribUid = SMSAttributeValue.getAttribute();
    String val = SMSAttributeValue.getValue();

    TrackedEntityAttribute attribute =
        trackedEntityAttributeService.getTrackedEntityAttribute(attribUid.getUid());

    if (attribute == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_ATTRIB.set(attribUid));
    } else if (val == null) {
      // TODO: Is this an error we can't recover from?
      throw new SMSProcessingException(SmsResponse.NULL_ATTRIBVAL.set(attribUid));
    }
    TrackedEntityAttributeValue trackedEntityAttributeValue = new TrackedEntityAttributeValue();
    trackedEntityAttributeValue.setAttribute(attribute);
    trackedEntityAttributeValue.setTrackedEntity(te);
    trackedEntityAttributeValue.setValue(val);
    return trackedEntityAttributeValue;
  }

  protected List<Object> processEvent(
      SmsEvent event, User user, Enrollment enrollment, IncomingSms sms) {
    Uid stageid = event.getProgramStage();
    Uid aocid = event.getAttributeOptionCombo();
    Uid orgunitid = event.getOrgUnit();

    OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit(orgunitid.getUid());
    if (orgUnit == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_ORGUNIT.set(orgunitid));
    }

    ProgramStage programStage = programStageService.getProgramStage(stageid.getUid());
    if (programStage == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_STAGE.set(stageid));
    }

    CategoryOptionCombo aoc = categoryService.getCategoryOptionCombo(aocid.getUid());
    if (aoc == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_AOC.set(aocid));
    }

    List<Object> errorUIDs =
        saveNewEvent(
            event.getEvent().getUid(),
            orgUnit,
            programStage,
            enrollment,
            sms,
            aoc,
            user,
            event.getValues(),
            event.getEventStatus(),
            event.getEventDate(),
            event.getDueDate(),
            event.getCoordinates());

    return errorUIDs;
  }
}
