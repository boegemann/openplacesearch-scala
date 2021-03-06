package org.iglootools.openplacesearch.api.language

import org.scalatest.junit.JUnitRunner

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.iglootools.openplacesearch.samples.Languages._
import org.iglootools.openplacesearch.api.language._
import org.iglootools.commons.scala.{SanityChecks}

@RunWith(classOf[JUnitRunner])
class LanguageSpec extends Spec with ShouldMatchers {
  describe("constructor") {
    it("should require a non-empty alpha3 code") {
      evaluating { Language(name="Hawaiian", alpha3Code="") } should produce [IllegalArgumentException]
    }

    it("should require a 3-character alpha3 code") {
      evaluating { Language(name="French", alpha3Code = "fr") } should produce[IllegalArgumentException]
    }

    it("should require a non-empty name") {
      evaluating { Language(name="", alpha3Code="haw") } should produce [IllegalArgumentException]
    }

    it("should require a non-null alpha2 code") {
      evaluating { Language(name="Hawaiian", "haw", null) } should produce [IllegalArgumentException]
    }

    it("should require a 2-character alpha2 code") {
      evaluating { Language(name="French", alpha3Code = "fra", alpha2Code=Some("fra")) } should produce[IllegalArgumentException]
    }
  }

  describe("toString") {
    it("should return name, alpha3 and alpha2 for French") {
      french.toString should be === "Language{name=French, alpha3=fra, alpha2=Some(fr)}"
    }

    it("should return name, alpha3 for Hawaiian") {
      hawaiian.toString should be === "Language{name=Hawaiian, alpha3=haw, alpha2=None}"
    }
  }

  describe("hashCode and equals") {
    it("should be sane") {
      SanityChecks.hashCodeAndEqualsShouldBeSane(french, eqObj=french, neObj=hawaiian)
    }
  }
}