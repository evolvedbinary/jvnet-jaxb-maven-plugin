/*
 * Copyright [2006] java.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jvnet.jaxb2.maven2;

import com.sun.xml.txw2.annotation.XmlNamespace;

import jakarta.xml.bind.annotation.XmlSchema;

/**
 * Maven JAXB 3.x Mojo.
 *
 * @author Adam Retter (adam@evolvedbinary.com)
 * @param <O>
 *        type
 */
public abstract class RawXJC3Mojo <O> extends RawXJCMojo <O, XmlSchema, XmlNamespace>
{
  private static final String JAXB_NSURI = "https://jakarta.ee/xml/ns/jaxb";

  @Override
  protected String getJaxbNamespaceUri ()
  {
    return JAXB_NSURI;
  }

  @Override
  protected String getBindPackageInfoClassName ()
  {
    return "com.sun.tools.xjc.reader.xmlschema.bindinfo.package-info";
  }

  @Override
  protected Class <XmlSchema> getXmlSchemaAnnotationClass ()
  {
    return XmlSchema.class;
  }

  @Override
  protected String getXmlSchemaAnnotationNamespace (final XmlSchema xmlSchemaAnnotation)
  {
    return xmlSchemaAnnotation.namespace ();
  }

  @Override
  protected String getEpisodePackageInfoClassName ()
  {
    return "org.glassfish.jaxb.core.v2.schemagen.episode.package-info";
  }

  @Override
  protected Class getXmlNamespaceAnnotationClass ()
  {
    return XmlNamespace.class;
  }

  @Override
  protected String getXmlNamespaceAnnotationValue (final XmlNamespace xmlValueAnnotation)
  {
    return xmlValueAnnotation.value ();
  }
}
