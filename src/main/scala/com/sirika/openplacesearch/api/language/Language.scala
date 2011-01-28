package com.sirika.openplacesearch.api.language

import com.google.common.base.Objects
import com.google.common.base.Strings

object Language {
    def apply(name : String, alpha3Code : String, alpha2Code : Option[String]=None) = new Language(name, alpha3Code, alpha2Code)
    def unapply(l : Language) = Some(l.name, l.alpha3Code, l.alpha2Code )
}

/**
 * ISO 639 Language (639-1 -alpha2- and 639-2 -alpha3-)
 * 
 * Codes are written lowercase !!! And ISO 639-2 codes are not considered here
 * 
 * @param alpha2Code <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>
 * @param alpha3Code <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-3</a>
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 * @see <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>
 * @see <a href="http://en.wikipedia.org/wiki/ISO_639-3">ISO 639-3</a>
 */
final class Language(val name : String, val alpha3Code : String, val alpha2Code : Option[String]) {
    require(!Strings.isNullOrEmpty(alpha3Code))
    require(!Strings.isNullOrEmpty(name))
    require(alpha2Code != null)
    
    override def hashCode(): Int = Objects.hashCode(alpha3Code)
    
    override def equals(other: Any): Boolean = other match {
        case Language(_, `alpha3Code`, _) => true
        case _ => false
    }
    
    override def toString() : String = Objects.toStringHelper(this)
        .add("name", name)    
        .add("alpha3", alpha3Code)
        .add("alpha2", alpha2Code)
        .toString()
}