package com.sirika.openplacesearch.api.formatter

import com.sirika.openplacesearch.api.commons.FeatureNameProvider

/**
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */

trait NameFormatter {
  def formatName(nameProvider: FeatureNameProvider)
}