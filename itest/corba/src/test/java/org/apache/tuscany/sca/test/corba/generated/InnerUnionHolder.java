package org.apache.tuscany.sca.test.corba.generated;

/**
* org/apache/tuscany/sca/test/corba/generated/InnerUnionHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from itest_scenario.idl
* niedziela, 17 sierpie� 2008 19:07:14 CEST
*/

public final class InnerUnionHolder implements org.omg.CORBA.portable.Streamable
{
  public org.apache.tuscany.sca.test.corba.generated.InnerUnion value = null;

  public InnerUnionHolder ()
  {
  }

  public InnerUnionHolder (org.apache.tuscany.sca.test.corba.generated.InnerUnion initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.apache.tuscany.sca.test.corba.generated.InnerUnionHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.apache.tuscany.sca.test.corba.generated.InnerUnionHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.apache.tuscany.sca.test.corba.generated.InnerUnionHelper.type ();
  }

}
