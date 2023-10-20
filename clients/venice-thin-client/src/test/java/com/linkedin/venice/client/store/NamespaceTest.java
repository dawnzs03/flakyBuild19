/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.linkedin.venice.client.store;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class NamespaceTest extends org.apache.avro.specific.SpecificRecordBase
    implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse(
      "{\"type\":\"record\",\"name\":\"NamespaceTest\",\"namespace\":\"com.linkedin.venice.client.store\",\"fields\":[{\"name\":\"foo\",\"type\":{\"type\":\"enum\",\"name\":\"EnumType\",\"symbols\":[\"A\",\"B\"]}},{\"name\":\"boo\",\"type\":\"string\"}]}");

  public static org.apache.avro.Schema getClassSchema() {
    return SCHEMA$;
  }

  @Deprecated
  public com.linkedin.venice.client.store.EnumType foo;
  @Deprecated
  public java.lang.CharSequence boo;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public NamespaceTest() {
  }

  /**
   * All-args constructor.
   */
  public NamespaceTest(com.linkedin.venice.client.store.EnumType foo, java.lang.CharSequence boo) {
    this.foo = foo;
    this.boo = boo;
  }

  public org.apache.avro.Schema getSchema() {
    return SCHEMA$;
  }

  // Used by DatumWriter. Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
      case 0:
        return foo;
      case 1:
        return boo;
      default:
        throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader. Applications should not call.
  @SuppressWarnings(value = "unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
      case 0:
        foo = (com.linkedin.venice.client.store.EnumType) value$;
        break;
      case 1:
        boo = (java.lang.CharSequence) value$;
        break;
      default:
        throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'foo' field.
   */
  public com.linkedin.venice.client.store.EnumType getFoo() {
    return foo;
  }

  /**
   * Sets the value of the 'foo' field.
   * @param value the value to set.
   */
  public void setFoo(com.linkedin.venice.client.store.EnumType value) {
    this.foo = value;
  }

  /**
   * Gets the value of the 'boo' field.
   */
  public java.lang.CharSequence getBoo() {
    return boo;
  }

  /**
   * Sets the value of the 'boo' field.
   * @param value the value to set.
   */
  public void setBoo(java.lang.CharSequence value) {
    this.boo = value;
  }

  /** Creates a new NamespaceTest RecordBuilder */
  public static com.linkedin.venice.client.store.NamespaceTest.Builder newBuilder() {
    return new com.linkedin.venice.client.store.NamespaceTest.Builder();
  }

  /** Creates a new NamespaceTest RecordBuilder by copying an existing Builder */
  public static com.linkedin.venice.client.store.NamespaceTest.Builder newBuilder(
      com.linkedin.venice.client.store.NamespaceTest.Builder other) {
    return new com.linkedin.venice.client.store.NamespaceTest.Builder(other);
  }

  /** Creates a new NamespaceTest RecordBuilder by copying an existing NamespaceTest instance */
  public static com.linkedin.venice.client.store.NamespaceTest.Builder newBuilder(
      com.linkedin.venice.client.store.NamespaceTest other) {
    return new com.linkedin.venice.client.store.NamespaceTest.Builder(other);
  }

  /**
   * RecordBuilder for NamespaceTest instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<NamespaceTest>
      implements org.apache.avro.data.RecordBuilder<NamespaceTest> {
    private com.linkedin.venice.client.store.EnumType foo;
    private java.lang.CharSequence boo;

    /** Creates a new Builder */
    private Builder() {
      super(com.linkedin.venice.client.store.NamespaceTest.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(com.linkedin.venice.client.store.NamespaceTest.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.foo)) {
        this.foo = data().deepCopy(fields()[0].schema(), other.foo);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.boo)) {
        this.boo = data().deepCopy(fields()[1].schema(), other.boo);
        fieldSetFlags()[1] = true;
      }
    }

    /** Creates a Builder by copying an existing NamespaceTest instance */
    private Builder(com.linkedin.venice.client.store.NamespaceTest other) {
      super(com.linkedin.venice.client.store.NamespaceTest.SCHEMA$);
      if (isValidValue(fields()[0], other.foo)) {
        this.foo = data().deepCopy(fields()[0].schema(), other.foo);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.boo)) {
        this.boo = data().deepCopy(fields()[1].schema(), other.boo);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'foo' field */
    public com.linkedin.venice.client.store.EnumType getFoo() {
      return foo;
    }

    /** Sets the value of the 'foo' field */
    public com.linkedin.venice.client.store.NamespaceTest.Builder setFoo(
        com.linkedin.venice.client.store.EnumType value) {
      validate(fields()[0], value);
      this.foo = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'foo' field has been set */
    public boolean hasFoo() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'foo' field */
    public com.linkedin.venice.client.store.NamespaceTest.Builder clearFoo() {
      foo = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'boo' field */
    public java.lang.CharSequence getBoo() {
      return boo;
    }

    /** Sets the value of the 'boo' field */
    public com.linkedin.venice.client.store.NamespaceTest.Builder setBoo(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.boo = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /** Checks whether the 'boo' field has been set */
    public boolean hasBoo() {
      return fieldSetFlags()[1];
    }

    /** Clears the value of the 'boo' field */
    public com.linkedin.venice.client.store.NamespaceTest.Builder clearBoo() {
      boo = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public NamespaceTest build() {
      try {
        NamespaceTest record = new NamespaceTest();
        record.foo =
            fieldSetFlags()[0] ? this.foo : (com.linkedin.venice.client.store.EnumType) defaultValue(fields()[0]);
        record.boo = fieldSetFlags()[1] ? this.boo : (java.lang.CharSequence) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
