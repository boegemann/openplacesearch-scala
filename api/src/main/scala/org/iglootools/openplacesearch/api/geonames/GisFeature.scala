package org.iglootools.openplacesearch.api.geonames

import org.iglootools.openplacesearch.api.administrativedivision.AdministrativeEntity
import org.joda.time.DateTimeZone
import com.vividsolutions.jts.geom.Point
import org.iglootools.openplacesearch.api.language.Language
import org.iglootools.openplacesearch.api.feature._
import org.iglootools.openplacesearch.api.feature.FeatureGeographyProvider

/**
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */

protected[openplacesearch] case class GisFeature(protected[this] val featureGeography: FeatureGeographyProvider,
                      protected[this] val geonamesFeature: GeonamesFeatureProvider,
                      protected[this] val featureNames: FeatureNames,
                      val parentAdministrativeEntity: Option[AdministrativeEntity] ) extends DistanceCalculator
  with GeonamesFeatureProvider
  with FeatureNameProvider
  with FeatureGeographyProvider
  with ParentAdministrativeEntityProvider
  with StableIdProvider {

  // GeonamesFeatureProvider
  def geonamesId: Long = geonamesFeature.geonamesId
  def geonamesFeatureCategory: GeonamesFeatureCategory = geonamesFeature.geonamesFeatureCategory

  // FeatureGeographyProvider : delegation to featureGeography
  def location: Point = featureGeography.location
  def population: Option[Long] = featureGeography.population
  def gTopo30ElevationInMeters: Option[Long] = featureGeography.gTopo30ElevationInMeters
  def elevationInMeters: Option[Long] = featureGeography.elevationInMeters
  def timeZone: Option[DateTimeZone] = featureGeography.timeZone

  // FeatureNameProvider : delegation to featureNames
  def userFriendlyCode: Option[String] = None
  def name:String = featureNames.defaultName
  def localizedNames:List[LocalizedName] = featureNames.localizedNames
  def preferredName(language: Language):String = featureNames.preferredName(language)
  def shortName(language: Language): String = featureNames.shortName(language)

  // StableIdProvider
  def stableId: String = geonamesFeature.stableId
}
