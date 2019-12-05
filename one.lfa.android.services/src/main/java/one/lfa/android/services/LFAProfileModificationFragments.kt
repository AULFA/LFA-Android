package one.lfa.android.services

import org.nypl.simplified.ui.profiles.ProfileModificationAbstractFragment
import org.nypl.simplified.ui.profiles.ProfileModificationFragmentParameters
import org.nypl.simplified.ui.profiles.ProfileModificationFragmentServiceType

class LFAProfileModificationFragments : ProfileModificationFragmentServiceType {
  override fun createModificationFragment(
    parameters: ProfileModificationFragmentParameters
  ): ProfileModificationAbstractFragment {
    return LFAProfileModificationFragment.create(parameters)
  }
}