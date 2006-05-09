package org.apache.tuscany.sdo.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.tuscany.sdo.SDOExtendedMetaData;
import org.apache.tuscany.sdo.util.SDOUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDComponent;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDFeature;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;
import org.w3c.dom.Element;

/**
 * TODO: 
 *  - Implement support for the SDO XSD Schema annotations
 *  - Override the default ecore type mappings
 *  
 * DONE:
 *  - Override the default XSDEcoreBuilder name mangling
 */
public class SDOXSDEcoreBuilder extends XSDEcoreBuilder
{

  public SDOXSDEcoreBuilder(ExtendedMetaData extendedMetaData)
  {
    super(extendedMetaData);
  }

  public EClassifier getEClassifier(XSDTypeDefinition xsdTypeDefinition) {
    EClassifier eClassifier = null;
    if (rootSchema.getSchemaForSchemaNamespace().equals(xsdTypeDefinition.getTargetNamespace())) {
      eClassifier = 
        getBuiltInEClassifier(
          xsdTypeDefinition.getURI(), 
          xsdTypeDefinition.getName());
    } else {
      eClassifier = super.getEClassifier(xsdTypeDefinition);
    }
    return eClassifier;
  }
  
  public EDataType getEDataType(XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
    EDataType eClassifier = null;
    if (rootSchema.getSchemaForSchemaNamespace().equals(xsdSimpleTypeDefinition.getTargetNamespace())) {
      eClassifier =
        (EDataType)getBuiltInEClassifier(
          xsdSimpleTypeDefinition.getURI(),
          xsdSimpleTypeDefinition.getName());
    } else {
      eClassifier = super.getEDataType(xsdSimpleTypeDefinition);
    }
    return (EDataType)eClassifier;
  }
  
  protected EClassifier getBuiltInEClassifier(String namespace, String name)
  {
    EClassifier eClassifier = (EClassifier)SDOUtil.getXSDSDOType(name);
    if (eClassifier == null) {
      eClassifier = super.getBuiltInEClassifier(namespace, name);
    }
    return eClassifier;
  }
  
  public EClass computeEClass(XSDComplexTypeDefinition xsdComplexTypeDefinition) {
    EClass eclass = super.computeEClass(xsdComplexTypeDefinition);
    String aliasNames = getEcoreAttribute(xsdComplexTypeDefinition.getElement(), "aliasName");
    if (aliasNames != null) {
      SDOExtendedMetaData.INSTANCE.setAliasNames(eclass, aliasNames);
    }
    return eclass;
  }

  protected EClassifier computeEClassifier(XSDTypeDefinition xsdTypeDefinition) {
    EClassifier eclassifier = super.computeEClassifier(xsdTypeDefinition);
    EClassifier etype = (EClassifier) typeToTypeObjectMap.get(eclassifier);
    String aliasNames = getEcoreAttribute(xsdTypeDefinition.getElement(), "aliasName");
    if (aliasNames != null) {
      SDOExtendedMetaData.INSTANCE.setAliasNames(eclassifier, aliasNames);
      if (etype != null) {
        SDOExtendedMetaData.INSTANCE.setAliasNames(etype, aliasNames);
      }
    }
    return eclassifier;
  }

  protected EDataType computeEDataType(XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
    EDataType edatatype = super.computeEDataType(xsdSimpleTypeDefinition);
    String aliasNames = getEcoreAttribute(xsdSimpleTypeDefinition.getElement(), "aliasName");
    if (aliasNames != null) {
      SDOExtendedMetaData.INSTANCE.setAliasNames(edatatype, aliasNames);
    }
    return edatatype;
  }

  protected EEnum computeEEnum(XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
    return null;
  }
    
  protected EStructuralFeature createFeature(EClass eClass, String name, EClassifier type, XSDComponent xsdComponent, int minOccurs, int maxOccurs) {
    EStructuralFeature feature = 
      super.createFeature(eClass, name, type, xsdComponent, minOccurs, maxOccurs);
    feature.setName(name); // this is needed because super.createFeature() does EMF name mangling (toLower)
    if (xsdComponent != null) {
      String aliasNames = getEcoreAttribute(xsdComponent.getElement(), "aliasName");
      if (aliasNames != null) {
        SDOExtendedMetaData.INSTANCE.setAliasNames(feature, aliasNames);
      }
    }
    return feature;
  }

  protected String getInstanceClassName(XSDTypeDefinition typeDefinition, EDataType baseEDataType) {
    String name = getEcoreAttribute(typeDefinition, "extendedInstanceClass");
    return (name != null) ? name : super.getInstanceClassName(typeDefinition, baseEDataType);
  }
  
  protected String getEcoreAttribute(Element element, String attribute)
  {
    String sdoAttribute = null;

    if ("name".equals(attribute))
      sdoAttribute = "name";
    else if ("opposite".equals(attribute))
      sdoAttribute = "oppositeProperty";
    else if ("mixed".equals(attribute))
      sdoAttribute = "sequence";
    else if ("string".equals(attribute))
      sdoAttribute = "string";
    else if ("changeable".equals(attribute))
      sdoAttribute = "readOnly";
    else if ("aliasName".equals(attribute))
      sdoAttribute = "aliasName";
    
    if (sdoAttribute != null)
    {
      String value = 
        element != null && element.hasAttributeNS("commonj.sdo/xml", sdoAttribute) ? 
          element.getAttributeNS("commonj.sdo/xml", sdoAttribute) : 
          null;
      if ("changeable".equals(attribute)) {
        if ("true".equals(value)) value = "false";
        else if ("false".equals(value)) value = "true";
      }
      return value;
    }
    
    if ("package".equals(attribute))
      sdoAttribute = "package";
    else if ("instanceClass".equals(attribute))
      sdoAttribute = "instanceClass";
    else if ("extendedInstanceClass".equals(attribute))
      sdoAttribute = "extendedInstanceClass";
    else if ("nestedInterfaces".equals(attribute))
      sdoAttribute = "nestedInterfaces";
    
    if (sdoAttribute != null)
    {
      return 
        element != null && element.hasAttributeNS("commonj.sdo/java", sdoAttribute) ? 
          element.getAttributeNS("commonj.sdo/java", sdoAttribute) : 
          null;
    }

    return super.getEcoreAttribute(element, attribute);
  }

  protected String getEcoreAttribute(XSDConcreteComponent xsdConcreteComponent, String attribute)
  {
    String value = super.getEcoreAttribute(xsdConcreteComponent, attribute);
    if ("package".equals(attribute) && value == null)
    {
      XSDSchema xsdSchema = (XSDSchema)xsdConcreteComponent;
      value = getDefaultPackageName(xsdSchema.getTargetNamespace());
    }
    return value;
  }
  
  protected XSDTypeDefinition getEcoreTypeQNameAttribute(XSDConcreteComponent xsdConcreteComponent, String attribute)
  {    
    if (xsdConcreteComponent == null) return null;
    String sdoAttribute = null;

    if ("reference".equals(attribute)) sdoAttribute = "propertyType";
    if ("dataType".equals(attribute)) sdoAttribute = "dataType";
    
    if (sdoAttribute != null)
    {
      Element element = xsdConcreteComponent.getElement();
      return  element == null ? null : getEcoreTypeQNameAttribute(xsdConcreteComponent, element, "commonj.sdo/xml", sdoAttribute);
    }

    return super.getEcoreTypeQNameAttribute(xsdConcreteComponent, attribute);
  }
   
  /**
   * Override default EMF behavior so that the name is not mangled.
   */
  protected String validName(String name, int casing, String prefix) {
    return name; 
  }

  /**
  * Override default EMF name mangling for anonymous types (simple and complex)
  */
  protected String validAliasName(XSDTypeDefinition xsdTypeDefinition, boolean isUpperCase) {
    return getAliasName(xsdTypeDefinition);
  }

  protected String getAliasName(XSDNamedComponent xsdNamedComponent) {
    String result = xsdNamedComponent.getName();
    if (result == null)
    {
      XSDConcreteComponent container = xsdNamedComponent.getContainer();
      if (container instanceof XSDNamedComponent)
      {
        result = getAliasName((XSDNamedComponent)container);
      }
    }
    return result;
  }
  
  protected XSDTypeDefinition getEffectiveTypeDefinition(XSDComponent xsdComponent, XSDFeature xsdFeature) {
    XSDTypeDefinition typeDef = getEcoreTypeQNameAttribute(xsdComponent, "dataType");

    String isString = getEcoreAttribute(xsdComponent, xsdFeature, "string");
    if ("true".equalsIgnoreCase(isString)) {
      typeDef = 
        xsdFeature.resolveSimpleTypeDefinition(rootSchema.getSchemaForSchemaNamespace(), "string");
    }
    if (typeDef == null)
      typeDef = xsdFeature.getType();
    return typeDef;
  }

  //Code below here to provide common URI to java packagname
  
  public static String uncapNameStatic(String name)
  {
    if (name.length() == 0)
    {
      return name;
    }
    else
    {
      String lowerName = name.toLowerCase();
      int i;
      for (i = 0; i < name.length(); i++)
      {
        if (name.charAt(i) == lowerName.charAt(i))
        {
          break;
        }
      }
      if (i > 1 && i < name.length() && !Character.isDigit(name.charAt(i)))
      {
        --i;
      }
      return name.substring(0, i).toLowerCase() + name.substring(i);
    }
  }

protected static String validNameStatic(String name, int casing, String prefix)
  {
    List parsedName = parseNameStatic(name, '_');
    StringBuffer result = new StringBuffer();
    for (Iterator i = parsedName.iterator(); i.hasNext(); )
    {
      String nameComponent = (String)i.next();
      if (nameComponent.length() > 0)
      {
        if (result.length() > 0 || casing == UPPER_CASE)
        {
          result.append(Character.toUpperCase(nameComponent.charAt(0)));
          result.append(nameComponent.substring(1));
        }
        else
        {
          result.append(nameComponent);
        }
      }
    }

    return
      result.length() == 0 ?
        prefix :
        Character.isJavaIdentifierStart(result.charAt(0)) ?
          casing == LOWER_CASE ?
            uncapNameStatic(result.toString()) :
            result.toString() :
          prefix + result;
  }

  protected static List parseNameStatic(String sourceName, char separator)
  {
    List result = new ArrayList();
    if (sourceName != null)
    {
      StringBuffer currentWord = new StringBuffer();
      boolean lastIsLower = false;
      for (int index = 0, length = sourceName.length(); index < length; ++index)
      {
        char curChar = sourceName.charAt(index);
        if (!Character.isJavaIdentifierPart(curChar))
        {
          curChar = separator;
        }
        if (Character.isUpperCase(curChar) || (!lastIsLower && Character.isDigit(curChar)) || curChar == separator)
        {
          if (lastIsLower && currentWord.length() > 1 || curChar == separator && currentWord.length() > 0)
          {
            result.add(currentWord.toString());
            currentWord = new StringBuffer();
          }
          lastIsLower = false;
        }
        else
        {
          if (!lastIsLower)
          {
            int currentWordLength = currentWord.length();
            if (currentWordLength > 1)
            {
              char lastChar = currentWord.charAt(--currentWordLength);
              currentWord.setLength(currentWordLength);
              result.add(currentWord.toString());
              currentWord = new StringBuffer();
              currentWord.append(lastChar);
            }
          }
          lastIsLower = true;
        }

        if (curChar != separator)
        {
          currentWord.append(curChar);
        }
      }

      result.add(currentWord.toString());
    }
    return result;
  }

public static String getDefaultPackageName(String targetNamespace)
  {

      URI uri = URI.createURI(targetNamespace);
      List parsedName;
      if (uri.isHierarchical())
      {
        String host = uri.host();
        if (host != null && host.startsWith("www."))
        {
          host = host.substring(4);
        }
        parsedName = parseNameStatic(host, '.');
        Collections.reverse(parsedName);
        if (!parsedName.isEmpty())
        {
          parsedName.set(0, ((String)parsedName.get(0)).toLowerCase());
        }
  
        parsedName.addAll(parseNameStatic(uri.trimFileExtension().path(), '/'));
      }
      else
      {
        String opaquePart = uri.opaquePart();
        int index = opaquePart.indexOf(":");
        if (index != -1 && "urn".equalsIgnoreCase(uri.scheme()))
        {
          parsedName = parseNameStatic(opaquePart.substring(0, index), '-');
          if (parsedName.size() > 0 && DOMAINS.contains(parsedName.get(parsedName.size() - 1))) 
          {
            Collections.reverse(parsedName);
            parsedName.set(0, ((String)parsedName.get(0)).toLowerCase());
          }
          parsedName.addAll(parseNameStatic(opaquePart.substring(index + 1), '/'));
        }
        else
        {
          parsedName = parseNameStatic(opaquePart, '/');
        }
      }

      StringBuffer qualifiedPackageName = new StringBuffer();
      for (Iterator i = parsedName.iterator(); i.hasNext(); )
      {
        String packageName = (String)i.next();
        if (packageName.length() > 0)
        {
          if (qualifiedPackageName.length() > 0)
          {
            qualifiedPackageName.append('.');
          }
          qualifiedPackageName.append(validNameStatic(packageName, LOWER_CASE,"_"));
        }
      }
    
    return qualifiedPackageName.toString().toLowerCase(); //make sure it's lower case .. we can't work with Axis if not.
  }
 

}