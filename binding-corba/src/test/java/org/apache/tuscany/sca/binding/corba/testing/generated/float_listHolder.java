package org.apache.tuscany.sca.binding.corba.testing.generated;


/**
* Tester/float_listHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from tester.idl
* pi�tek, 30 maj 2008 17:04:42 CEST
*/

public final class float_listHolder implements org.omg.CORBA.portable.Streamable
{
  public float value[] = null;

  public float_listHolder ()
  {
  }

  public float_listHolder (float[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.apache.tuscany.sca.binding.corba.testing.generated.float_listHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.apache.tuscany.sca.binding.corba.testing.generated.float_listHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.apache.tuscany.sca.binding.corba.testing.generated.float_listHelper.type ();
  }

}
