package one.lfa.android.services

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import com.io7m.jfunctional.None
import com.io7m.jfunctional.OptionType
import com.io7m.jfunctional.Some
import com.io7m.junreachable.UnreachableCodeException
import io.reactivex.disposables.Disposable
import one.irradia.datepicker.views.DatePicker3P
import one.lfa.android.services.LFAWhitespace.WHITESPACE
import one.lfa.android.services.LFAWhitespace.isWhitespace
import org.joda.time.DateTime
import org.librarysimplified.services.api.Services
import org.nypl.simplified.accounts.registry.api.AccountProviderRegistryType
import org.nypl.simplified.navigation.api.NavigationControllers
import org.nypl.simplified.profiles.api.ProfileAttributes
import org.nypl.simplified.profiles.api.ProfileAttributes.Companion.GENDER_ATTRIBUTE_KEY
import org.nypl.simplified.profiles.api.ProfileAttributes.Companion.GRADE_ATTRIBUTE_KEY
import org.nypl.simplified.profiles.api.ProfileAttributes.Companion.ROLE_ATTRIBUTE_KEY
import org.nypl.simplified.profiles.api.ProfileAttributes.Companion.SCHOOL_ATTRIBUTE_KEY
import org.nypl.simplified.profiles.api.ProfileCreationEvent
import org.nypl.simplified.profiles.api.ProfileDateOfBirth
import org.nypl.simplified.profiles.api.ProfileDescription
import org.nypl.simplified.profiles.api.ProfileEvent
import org.nypl.simplified.profiles.api.ProfilePreferences
import org.nypl.simplified.profiles.api.ProfileUpdated
import org.nypl.simplified.profiles.controller.api.ProfilesControllerType
import org.nypl.simplified.reader.api.ReaderPreferences
import org.nypl.simplified.ui.profiles.ProfileModificationAbstractFragment
import org.nypl.simplified.ui.profiles.ProfileModificationFragmentParameters
import org.nypl.simplified.ui.profiles.ProfilesNavigationControllerType
import org.nypl.simplified.ui.thread.api.UIThreadServiceType
import org.slf4j.LoggerFactory
import java.util.Locale

class LFAProfileModificationFragment : ProfileModificationAbstractFragment() {

  companion object {

    private const val PARAMETERS_ID =
      "one.lfa.android.services.LFAModificationFragment.parameters"

    /**
     * Create a fragment for the given parameters.
     */

    fun create(parameters: ProfileModificationFragmentParameters): LFAProfileModificationFragment {
      val arguments = Bundle()
      arguments.putSerializable(this.PARAMETERS_ID, parameters)
      val fragment = LFAProfileModificationFragment()
      fragment.arguments = arguments
      return fragment
    }
  }

  private lateinit var date: DatePicker3P
  private lateinit var finishButton: Button
  private lateinit var genderNonBinaryEditText: EditText
  private lateinit var genderNonBinaryRadioButton: RadioButton
  private lateinit var genderRadioGroup: RadioGroup
  private lateinit var gradeLayout: ViewGroup
  private lateinit var gradeSpinner: Spinner
  private lateinit var nameFirst: EditText
  private lateinit var nameLast: EditText
  private lateinit var onChange: OnTextChangeListener
  private lateinit var parameters: ProfileModificationFragmentParameters
  private lateinit var pilotSchoolLayout: ViewGroup
  private lateinit var pilotSchoolRadioGroup: RadioGroup
  private lateinit var pilotSchoolSpinner: Spinner
  private lateinit var profilesController: ProfilesControllerType
  private lateinit var registry: AccountProviderRegistryType
  private lateinit var roleEditText: EditText
  private lateinit var roleOtherRadioButton: RadioButton
  private lateinit var roleRadioGroup: RadioGroup
  private lateinit var title: TextView
  private lateinit var uiThread: UIThreadServiceType
  private val logger = LoggerFactory.getLogger(LFAProfileModificationFragment::class.java)
  private val parametersId = PARAMETERS_ID
  private var profileSubscription: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.parameters =
      this.requireArguments()[this.parametersId] as ProfileModificationFragmentParameters

    this.onChange = OnTextChangeListener(this::onSomethingChanged)

    val services = Services.serviceDirectory()

    this.profilesController =
      services.requireService(ProfilesControllerType::class.java)
    this.uiThread =
      services.requireService(UIThreadServiceType::class.java)
    this.registry =
      services.requireService(AccountProviderRegistryType::class.java)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val layout =
      inflater.inflate(R.layout.lfa_profile_mod, container, false)

    this.title = layout.findViewById(R.id.profileCreateTitle)
    if (this.parameters.profileID != null) {
      this.title.setText(R.string.profileModifyTitle)
    }

    this.finishButton = layout.findViewById(R.id.profileCreationCreate)
    this.finishButton.isEnabled = false
    this.finishButton.setOnClickListener { view ->
      view.isEnabled = false
      this.createOrModifyProfile()
    }

    this.date =
      layout.findViewById(R.id.profileCreationDateSelection)
    this.nameFirst =
      layout.findViewById(R.id.profileCreationEditNameFirst)
    this.nameLast =
      layout.findViewById(R.id.profileCreationEditNameLast)

    this.genderRadioGroup =
      layout.findViewById(R.id.profileGenderRadioGroup)
    this.genderNonBinaryRadioButton =
      layout.findViewById(R.id.profileGenderNonBinaryRadioButton)
    this.genderNonBinaryEditText =
      layout.findViewById(R.id.profileGenderNonBinaryEditText)

    this.roleRadioGroup =
      layout.findViewById(R.id.profileRoleRadioGroup)
    this.roleOtherRadioButton =
      layout.findViewById(R.id.profileRoleOtherRadioButton)
    this.roleEditText =
      layout.findViewById(R.id.profileRoleEditText)

    this.roleEditText.filters =
      arrayOf<InputFilter>(LFAProfileRoleFilter())

    this.gradeLayout =
      layout.findViewById(R.id.profileGradeLayout)
    this.gradeSpinner =
      layout.findViewById(R.id.profileGradeSpinner)

    this.pilotSchoolLayout =
      layout.findViewById(R.id.profilePilotSchool)
    this.pilotSchoolRadioGroup =
      layout.findViewById(R.id.profilePilotSchoolRadioGroup)
    this.pilotSchoolSpinner =
      layout.findViewById(R.id.profilePilotSchoolYesSpinner)

    this.pilotSchoolRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      if (checkedId == R.id.profilePilotSchoolYesRadioButton) {
        this.pilotSchoolSpinner.isEnabled = true
      } else {
        this.pilotSchoolSpinner.isEnabled = false
        this.pilotSchoolSpinner.setSelection(-1)
      }
    }
    this.pilotSchoolRadioGroup.check(R.id.profilePilotSchoolNoRadioButton)

    this.nameFirst.addTextChangedListener(this.onChange)
    this.nameLast.addTextChangedListener(this.onChange)

    this.genderNonBinaryEditText.addTextChangedListener(this.onChange)
    this.genderNonBinaryEditText.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
      if (hasFocus) {
        this.genderNonBinaryRadioButton.isChecked = true
      }
    }
    this.genderRadioGroup.setOnCheckedChangeListener { _, id ->
      if (id == R.id.profileGenderNonBinaryRadioButton) {
        this.genderNonBinaryEditText.requestFocus()
      } else {
        this.genderNonBinaryEditText.clearFocus()
      }
      this.updateUIState()
    }

    this.roleEditText.addTextChangedListener(this.onChange)
    this.roleEditText.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
      if (hasFocus) {
        this.roleOtherRadioButton.isChecked = true
      }
    }
    this.roleRadioGroup.setOnCheckedChangeListener { _, id ->
      if (id == R.id.profileRoleOtherRadioButton) {
        this.roleEditText.requestFocus()
      } else {
        this.roleEditText.clearFocus()
      }
      this.updateUIState()
    }

    return layout
  }

  override fun onStart() {
    super.onStart()

    val fragmentActivity = this.requireActivity()
    val currentProfile =
      try {
        if (this.parameters.profileID != null) {
          this.profilesController.profiles()[this.parameters.profileID]
        } else {
          null
        }
      } catch (e: Exception) {
        this.logger.error("could not locate profile ${this.parameters.profileID}: ", e)
        null
      }

    if (currentProfile != null) {
      val namePair = FirstAndLastName.parse(currentProfile.displayName)
      this.nameFirst.setText(namePair.nameFirst)
      this.nameLast.setText(namePair.nameLast)

      val attributes = currentProfile.attributes()
      val preferences = currentProfile.preferences()
      val dateOfBirth = preferences.dateOfBirth
      if (dateOfBirth != null) {
        this.date.date = dateOfBirth.date.toLocalDate()
      }

      val gender = attributes.gender
      if (gender != null) {
        when (gender) {
          "male" -> this.genderRadioGroup.check(R.id.profileGenderMaleRadioButton)
          "female" -> this.genderRadioGroup.check(R.id.profileGenderFemaleRadioButton)
          else -> {
            this.genderRadioGroup.check(R.id.profileGenderNonBinaryRadioButton)
            this.genderNonBinaryEditText.setText(gender)
          }
        }
      }

      val role = attributes.role
      if (role != null) {
        when (role) {
          "parent" -> this.roleRadioGroup.check(R.id.profileRoleParetRadioButton)
          "student" -> this.roleRadioGroup.check(R.id.profileRoleStudentRadioButton)
          "teacher" -> this.roleRadioGroup.check(R.id.profileRoleTeacherRadioButton)
          else -> {
            this.roleRadioGroup.check(R.id.profileRoleOtherRadioButton)
            this.roleEditText.setText(role)
          }
        }
      }

      /*
       * Find the school in the pilot program spinner, if any.
       */

      val school = attributes.school
      if (school != null) {
        val adapter = this.pilotSchoolSpinner.adapter
        for (index in 0 until adapter.count) {
          val adapterItem = adapter.getItem(index).toString()
          if (adapterItem == school) {
            this.logger.debug("set school spinner to index {}", index)
            this.pilotSchoolSpinner.setSelection(index, true)
            this.pilotSchoolRadioGroup.check(R.id.profilePilotSchoolYesRadioButton)
            break
          }
        }
      }

      /*
       * Find the grade in the grade spinner, if any.
       */

      val grade = attributes.grade
      if (grade != null) {
        val adapter = this.gradeSpinner.adapter
        for (index in 0 until adapter.count) {
          val adapterItem = adapter.getItem(index).toString()
          if (adapterItem == grade) {
            this.logger.debug("set grade spinner to index {}", index)
            this.gradeSpinner.setSelection(index, true)
            break
          }
        }
      }

      this.finishButton.setText(R.string.profileModify)
    } else {
      this.finishButton.setText(R.string.profileCreate)
    }

    this.profileSubscription =
      this.profilesController.profileEvents()
        .subscribe(this::onProfileEvent)
  }

  private fun onProfileEvent(event: ProfileEvent) {
    return when (event) {
      is ProfileUpdated.Succeeded,
      is ProfileCreationEvent.ProfileCreationSucceeded -> {
        this.uiThread.runOnUIThread {
          try {
            NavigationControllers.find(
              this.requireActivity(),
              ProfilesNavigationControllerType::class.java)
              .popBackStack()
          } catch (e: Exception) {
            this.logger.error("could not pop backstack: ", e)
          }
        }
      }

      is ProfileUpdated.Failed -> {
        this.uiThread.runOnUIThread {
          this.showProfileUpdateError(event)
        }
      }

      is ProfileCreationEvent.ProfileCreationFailed -> {
        this.uiThread.runOnUIThread {
          this.showProfileCreationError(event)
        }
      }

      else -> {

      }
    }
  }

  @UiThread
  private fun showProfileCreationError(event: ProfileCreationEvent.ProfileCreationFailed) {
    this.uiThread.checkIsUIThread()

    val context = this.requireContext()
    AlertDialog.Builder(context)
      .setTitle(R.string.profileCreationError)
      .setMessage(when (event.errorCode()) {
        ProfileCreationEvent.ProfileCreationFailed.ErrorCode.ERROR_DISPLAY_NAME_ALREADY_USED ->
          context.getString(R.string.profileCreationErrorNameAlreadyUsed)
        null, ProfileCreationEvent.ProfileCreationFailed.ErrorCode.ERROR_GENERAL ->
          context.getString(R.string.profileCreationErrorGeneral)
      })
      .setIcon(R.drawable.profile_failure)
      .create()
      .show()
  }

  @UiThread
  private fun showProfileUpdateError(event: ProfileUpdated.Failed) {
    this.uiThread.checkIsUIThread()

    val context = this.requireContext()
    AlertDialog.Builder(context)
      .setTitle(R.string.profileUpdateError)
      .setMessage(context.getString(R.string.profileUpdateFailedMessage, event.exception.message))
      .setIcon(R.drawable.profile_failure)
      .create()
      .show()
  }

  private fun someOrEmpty(exception: OptionType<Exception>): String {
    return when (exception) {
      is Some<Exception> -> exception.get().message ?: ""
      is None -> ""
      else -> ""
    }
  }

  override fun onStop() {
    super.onStop()

    this.profileSubscription?.dispose()
    this.nameFirst.removeTextChangedListener(this.onChange)
    this.roleEditText.removeTextChangedListener(this.onChange)
    this.genderNonBinaryEditText.removeTextChangedListener(this.onChange)
  }

  data class FirstAndLastName(
    val nameFirst: String,
    val nameLast: String
  ) {
    val name: String =
      "${this.nameFirst} ${this.nameLast}"

    companion object {
      fun parse(rawName: String): FirstAndLastName {
        val segments = rawName.split(regex = WHITESPACE)
        return when (segments.size) {
          0 ->
            FirstAndLastName(
              nameFirst = "",
              nameLast = ""
            )
          1 ->
            FirstAndLastName(
              nameFirst = segments[0].trim(LFAWhitespace::isWhitespace),
              nameLast = ""
            )
          else -> {
            val firstNames =
              segments.take(segments.size - 1)
                .map { it.trim(LFAWhitespace::isWhitespace) }

            val firstName = firstNames.joinToString(" ")
            val lastName = segments.last()

            FirstAndLastName(
              nameFirst = firstName,
              nameLast = lastName
            )
          }
        }
      }
    }
  }

  private fun createOrModifyProfile() {
    val namePair = this.getNamePair()
    val genderText = this.getGenderText()
    val roleText = this.getRoleText()
    val school = this.getSchool()
    val grade = this.getGradeText()

    val readDate =
      this.date.date
    val dateValue =
      DateTime(readDate.year, readDate.monthOfYear, readDate.dayOfMonth, 0, 0)

    this.logger.debug("date:   {}", dateValue)
    this.logger.debug("gender: {}", genderText)
    this.logger.debug("grade:  {}", grade)
    this.logger.debug("name:   {} {}", namePair.nameFirst, namePair.nameLast)
    this.logger.debug("role:   {}", roleText)
    this.logger.debug("school: {}", school)

    val profile =
      this.parameters.profileID?.let { profileId ->
        this.profilesController.profiles()[profileId]
      }

    val oldPreferences =
      profile?.description()?.preferences
        ?: ProfilePreferences(
          dateOfBirth = ProfileDateOfBirth(
            date = dateValue,
            isSynthesized = false
          ),
          showTestingLibraries = false,
          readerPreferences = ReaderPreferences.builder().build(),
          mostRecentAccount = null,
          hasSeenLibrarySelectionScreen = true,
          useExperimentalR2 = false
        )

    val newPreferences =
      oldPreferences.copy(
        dateOfBirth = ProfileDateOfBirth(
          date = dateValue,
          isSynthesized = false
        ),
        useExperimentalR2 = false
      )

    val oldAttributes =
      profile?.attributes() ?: ProfileAttributes(sortedMapOf())

    val attributesMutable = oldAttributes.attributes.toMutableMap()
    attributesMutable[GENDER_ATTRIBUTE_KEY] = genderText
    attributesMutable[ROLE_ATTRIBUTE_KEY] = roleText
    if (school != null) {
      attributesMutable[SCHOOL_ATTRIBUTE_KEY] = school
    } else {
      attributesMutable.remove(SCHOOL_ATTRIBUTE_KEY)
    }
    if (grade != null) {
      attributesMutable[GRADE_ATTRIBUTE_KEY] = grade
    } else {
      attributesMutable.remove(GRADE_ATTRIBUTE_KEY)
    }

    val newAttributes =
      ProfileAttributes(attributesMutable.toSortedMap())

    val profileDescription =
      ProfileDescription(
        displayName = namePair.name,
        preferences = newPreferences,
        attributes = newAttributes
      )

    if (profile == null) {
      this.profilesController.profileCreate(
        accountProvider = this.registry.defaultProvider,
        description = profileDescription
      )
    } else {
      this.profilesController.profileUpdateFor(
        profile = profile.id,
        update = { profileDescription }
      )
    }
  }

  private fun getNamePair(): FirstAndLastName {
    return FirstAndLastName(
      nameFirst = this.nameFirst.text.toString(),
      nameLast = this.nameLast.text.toString()
    )
  }

  private fun getGradeText(): String? {
    return if (this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleStudentRadioButton) {
      this.gradeSpinner.selectedItem.toString()
    } else null
  }

  private fun getSchool(): String? {
    if (this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleStudentRadioButton) {
      if (this.pilotSchoolRadioGroup.checkedRadioButtonId == R.id.profilePilotSchoolYesRadioButton) {
        return this.pilotSchoolSpinner.selectedItem.toString()
      }
    }
    return null
  }

  private fun getGenderText(): String {
    return when {
      this.genderRadioGroup.checkedRadioButtonId == R.id.profileGenderFemaleRadioButton ->
        "female"
      this.genderRadioGroup.checkedRadioButtonId == R.id.profileGenderMaleRadioButton ->
        "male"
      this.genderRadioGroup.checkedRadioButtonId == R.id.profileGenderNonBinaryRadioButton ->
        this.genderNonBinaryEditText.text.toString()
          .toLowerCase(Locale.ROOT)
          .trim(LFAWhitespace::isWhitespace)

      else ->
        throw UnreachableCodeException()
    }
  }

  private fun getRoleText(): String {
    return when {
      this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleParetRadioButton ->
        "parent"
      this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleStudentRadioButton ->
        "student"
      this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleTeacherRadioButton ->
        "teacher"
      this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleOtherRadioButton ->
        this.roleEditText.text.toString()
          .toLowerCase(Locale.ROOT)
          .trim(LFAWhitespace::isWhitespace)
      else ->
        throw UnreachableCodeException()
    }
  }

  @UiThread
  private fun onSomethingChanged(
    sequence: CharSequence,
    start: Int,
    before: Int,
    count: Int
  ) {
    this.uiThread.checkIsUIThread()
    this.updateUIState()
  }

  @UiThread
  private fun updateUIState() {
    this.uiThread.checkIsUIThread()

    if (this.roleRadioGroup.checkedRadioButtonId == R.id.profileRoleStudentRadioButton) {
      this.pilotSchoolLayout.visibility = View.VISIBLE
      this.gradeLayout.visibility = View.VISIBLE
    } else {
      this.pilotSchoolLayout.visibility = View.GONE
      this.gradeLayout.visibility = View.GONE
    }

    this.updateFinishButton()
  }

  private fun updateFinishButton() {
    val isNameFirstOK =
      this.nameFirst.text.toString()
        .trim(LFAWhitespace::isWhitespace)
        .isNotEmpty()

    val isNameLastOK =
      this.nameLast.text.toString()
        .trim(LFAWhitespace::isWhitespace)
        .isNotEmpty()

    val isGenderNonBinaryEmpty =
      this.genderNonBinaryEditText.text.toString()
        .trim(LFAWhitespace::isWhitespace)
        .isEmpty()

    val isGenderAnyRadioButtonChecked =
      this.genderRadioGroup.checkedRadioButtonId != -1
    val isGenderNonBinaryRadioButtonChecked =
      this.genderNonBinaryRadioButton.isChecked

    val isGenderOk: Boolean
    if (isGenderAnyRadioButtonChecked) {
      if (isGenderNonBinaryRadioButtonChecked) {
        isGenderOk = !isGenderNonBinaryEmpty
      } else {
        isGenderOk = true
      }
    } else {
      isGenderOk = false
    }

    val isRoleOtherEmpty =
      this.roleEditText.text.toString()
        .trim(LFAWhitespace::isWhitespace)
        .isEmpty()

    val isRoleAnyRadioButtonChecked =
      this.roleRadioGroup.checkedRadioButtonId != -1
    val isRoleOtherRadioButtonChecked =
      this.roleOtherRadioButton.isChecked

    val isRoleOk =
      if (isRoleAnyRadioButtonChecked) {
        if (isRoleOtherRadioButtonChecked) {
          !isRoleOtherEmpty
        } else {
          true
        }
      } else {
        false
      }

    this.finishButton.isEnabled = isNameFirstOK && isNameLastOK && isRoleOk && isGenderOk
  }
}
