package com.sirika.openplacesearch.api.administrativedivision

/**
 * A FIPS Country Code
 * <p>
 * A country (as defined by ISO) might not have any associated FIPS Code.
 * It also happens that several ISO countries have the same FIPS code.
 * (e.g. : Finland, and aalen island ). In these cases, Finland would have
 * the original FIPS code, and aalen island would have this code as its
 * equivalent FIPS code
 * </p>
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */

final case class FipsCountryCode(val fipsCode: Option[String] = None, val equivalentFipsCode: Option[String] = None) {
  require(fipsCode != null, "fipsCode must be a non-null Option")
  require(equivalentFipsCode != null, "fipsCode must be a non-null Option")
}