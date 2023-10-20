package de.tum.in.www1.artemis.domain.metis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Custom constraint annotation for Posts.
 * It defines a validation constraint that Hibernate Validator will check
 * before Hibernate ORM persists or updates the entities using this annotation.
 */
@Constraint(validatedBy = { PostContextConstraintValidator.class })
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstraints {

    /**
     * @return the message of the violated constraint
     */
    String message() default "{de.tum.in.www1.artemis.domain.metis.PostConstraints}";

    /**
     * @return the groups, default is empty
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload, default is empty
     */
    Class<? extends Payload>[] payload() default {};
}
