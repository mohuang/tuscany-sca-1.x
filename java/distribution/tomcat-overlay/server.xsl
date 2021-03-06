<!--
  Copyright (c) 2005 The Apache Software Foundation or its licensors, as applicable.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 -->
<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output indent="yes" />
 
  <xsl:param name="classname"/> 
  <!-- xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template -->
  <xsl:template
  match="@* | * | comment() | processing-instruction() | text()">
    <xsl:copy>
      <xsl:apply-templates
      select="@* | * | comment() | processing-instruction() | text()" />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="Host">
    <Host>
  <xsl:attribute name="className">
    <xsl:text>org.apache.tuscany.tomcat.TuscanyHost</xsl:text>
  </xsl:attribute> 
    <xsl:apply-templates select="@*" />
   <xsl:apply-templates
      select="@* | * | comment() | processing-instruction() | text()" />
    </Host>
  </xsl:template>
  
  <!-- xsl:template match="/">
    <xsl:apply-templates select="node() | @*" />
  </xsl:template -->
</xsl:stylesheet>

