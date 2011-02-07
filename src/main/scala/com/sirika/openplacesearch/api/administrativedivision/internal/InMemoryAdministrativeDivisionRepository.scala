package com.sirika.openplacesearch.api.administrativedivision.internal

import com.google.common.io.{InputSupplier}
import java.io.InputStreamReader
import grizzled.slf4j.Logging
import com.sirika.openplacesearch.api.administrativedivision._
import com.sirika.commons.scala.{LineByLineInputStreamReader, Urls, ParsingWarning}

/**
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */

class InMemoryAdministrativeDivisionRepository extends AdministrativeDivisionRepository with Logging {
  private[this] val countryRepository = new InMemoryCountryRepository()
  private[this] object FirstOrderAdministrativeDivisions {
    val allFirstOrderAdministrativeDivisions = parseAdm1(Urls.toInputReaderSupplier("com/sirika/openplacesearch/api/administrativedivision/admin1CodesASCII"))
    val allFirstOrderAdministrativeDivisionsPerCountry: Map[Country,List[AdministrativeDivision]] = allFirstOrderAdministrativeDivisions.groupBy (_.country)
    val adm1LookupTable: Map[(Country,String),AdministrativeDivision] = Map(allFirstOrderAdministrativeDivisions.map{a : AdministrativeDivision => ((a.country,a.code), a)} : _*)

    private def parseAdm1(readerSupplier: InputSupplier[InputStreamReader]) : List[AdministrativeDivision] = {
      new LineByLineInputStreamReader(readerSupplier).map { (line, lineNumber) =>
        line.split('\t') match {
          case Array(compositeCode, name, asciiName, geonamesId)
          =>
            val Array(countryAlpha2Code,adminCode) = compositeCode.split('.')
            val parentAdministrativeEntity = countryRepository.getByIsoAlpha2Code(countryAlpha2Code)

            Right(AdministrativeDivision(
              code=adminCode,
              featureNameProvider=
                SimpleFeatureNameProvider(
                  defaultName = if(name.nonEmpty) name else asciiName,
                  parentAdministrativeEntity=Some(parentAdministrativeEntity)),
              parentAdministrativeEntityProvider=SimpleParentAdministrativeEntityProvider(Some(parentAdministrativeEntity))))
          case _ => throw new IllegalArgumentException("Syntax error in the input file. We are expecting the following fields: compositeCode, name, asciiName, geonamesId")
        }
      }
    }
  }

  private[this] object SecondOrderAdministrativeDivisions {
    val allSecondOrderAdministrativeDivisions = parseAdm2(Urls.toInputReaderSupplier("com/sirika/openplacesearch/api/administrativedivision/admin2Codes"))
    val allSecondOrderAdministrativeDivisionPerCountryAndAdm1: Map[(Country,AdministrativeDivision),List[AdministrativeDivision]] =
      allSecondOrderAdministrativeDivisions.groupBy {a=>(a.country,a.parentAdministrativeEntity.get.asInstanceOf[AdministrativeDivision])}
    val adm2LookupTable: Map[(Country,AdministrativeDivision, String),AdministrativeDivision] =
      Map(allSecondOrderAdministrativeDivisions.map{a : AdministrativeDivision =>
        ((a.country,a.parentAdministrativeEntity.get.asInstanceOf[AdministrativeDivision], a.code), a)} : _*)

    private def parseAdm2(readerSupplier: InputSupplier[InputStreamReader]) : List[AdministrativeDivision] = {
      var adm1hacks: Map[(Country, String), AdministrativeDivision] = Map()

      val result = new LineByLineInputStreamReader(readerSupplier).map { (line, lineNumber) =>
        line.split('\t') match {
          case Array(compositeCode, name, asciiName, geonamesId)
          =>
            val Array(countryAlpha2Code,adm1Code,adm2Code) = compositeCode.split('.')
            val country = countryRepository.getByIsoAlpha2Code(countryAlpha2Code)
            val adm1 = getFirstOrderAdministrativeDivisionOption(country, adm1Code)

            def adm1ToUse: Option[AdministrativeDivision] = {
              adm1 match {
                case None =>
                  warn("ADM1 with code %s from country %s does not exist. ADM2(%s,%s) will have a parent ADM1 with name: %s".format(adm1Code, countryAlpha2Code, adm2Code, name, adm1Code + "[HACK]"))
                  adm1hacks.get((country, adm1Code)) match {
                    case None =>
                      val a = AdministrativeDivision(code=adm1Code,featureNameProvider=
                        SimpleFeatureNameProvider(
                          defaultName = adm1Code + "[HACK]",
                          parentAdministrativeEntity=Some(country)),
                        parentAdministrativeEntityProvider=SimpleParentAdministrativeEntityProvider(Some(country)))

                      val t = ((a.country,a.code), a)
                      adm1hacks = adm1hacks + t
                      adm1hacks.get((country, adm1Code))
                    case s => s
                  }
                case s => s
              }

            }

            Right(
              AdministrativeDivision(
                code=adm2Code,
                featureNameProvider=
                  SimpleFeatureNameProvider(
                    defaultName = if(name.nonEmpty) name else asciiName,
                    parentAdministrativeEntity=adm1ToUse),
                parentAdministrativeEntityProvider=SimpleParentAdministrativeEntityProvider(adm1ToUse)))
          case _ => throw new IllegalArgumentException("Syntax error in the input file. We are expecting the following fields: compositeCode, name, asciiName, geonamesId")
        }
      }

      sumUpErrors(adm1hacks)
      result
    }
  }

  def sumUpErrors(adm1hacks: Map[(Country, String), AdministrativeDivision]) {
    val errorsByCountry = adm1hacks.values.groupBy(_.country)
    errorsByCountry.keys.foreach { c =>
      val missingAdm1s = errorsByCountry.get(c).get.map(_.code).mkString(",")
      warn("(Summary) Country[%s] misses the following ADM1: %s".format(c.isoCountryCode.alpha3Code, missingAdm1s))

    }
  }

  //private val fipsLookupTable : Map[String, Country] = Map(countries.filter{_.fipsCountryCode.fipsCode.isDefined}.map{c : Country => (c.fipsCountryCode.fipsCode.get, c)} : _*)
  //private val alpha2LookupTable : Map[String, Country] = Map(countries.map{c : Country => (c.isoCountryCode.alpha2Code, c)} : _*)
  //private val alpha3LookupTable : Map[String, Country] = Map(countries.map{c : Country => (c.isoCountryCode.alpha3Code, c)} : _*)

  def findAllSecondOrderAdministrativeDivisions(country: Country, firstOrderAdministrativeDivision: AdministrativeDivision) = {
    SecondOrderAdministrativeDivisions.allSecondOrderAdministrativeDivisionPerCountryAndAdm1.get((country, firstOrderAdministrativeDivision)).get
  }

  def getSecondOrderAdministrativeDivision(country: Country, firstOrderAdministrativeDivision: AdministrativeDivision, code: String) = {
    SecondOrderAdministrativeDivisions.adm2LookupTable.get((country, firstOrderAdministrativeDivision, code)).get
  }

  def findAllFirstOrderAdministrativeDivisions(country: Country) = {
    FirstOrderAdministrativeDivisions.allFirstOrderAdministrativeDivisionsPerCountry.get(country).get
  }

  def getFirstOrderAdministrativeDivision(country: Country, code: String): AdministrativeDivision = {
    FirstOrderAdministrativeDivisions.adm1LookupTable.get((country,code)).get
  }

  private def getFirstOrderAdministrativeDivisionOption(country: Country, code: String): Option[AdministrativeDivision] = {
    FirstOrderAdministrativeDivisions.adm1LookupTable.get((country,code))
  }

}