package com.example.mysec

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mysec.databinding.FragmentProjectBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_USER_ID = "user_id"
private const val TAG = "ProjectFragment"

class ProjectFragment : Fragment(), ScheduleDialogFragment.OnScheduleCreatedListener {

    private var _binding: FragmentProjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectRepository: ProjectRepository
    private lateinit var scheduleDatabaseHelper: ScheduleDatabaseHelper
    private lateinit var dbHelper: DBHelper
    private lateinit var projectDatabaseHelper: ProjectDatabaseHelper

    private var userId: String? = null
    private var currentProjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            Log.d(TAG, "User ID: $userId")
        }
        dbHelper = DBHelper(requireContext())
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
            ProjectFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Start")
        _binding = FragmentProjectBinding.inflate(inflater, container, false)
        projectRepository = ProjectRepository(requireContext())
        scheduleDatabaseHelper = ScheduleDatabaseHelper(requireContext())
        projectDatabaseHelper = ProjectDatabaseHelper(requireContext())

        userId = userId ?: dbHelper.getUserId()
        Log.d(TAG, "onCreateView: userId = $userId")

        if (userId.isNullOrEmpty()) {
            showToast("사용자 아이디가 설정되지 않았습니다.")
            Log.e(TAG, "onCreateView: 사용자 아이디가 설정되지 않았습니다.")
            return binding.root
        }

        // 프로젝트 세부 정보 로드 및 표시
        loadProjectDetails()
        updateScheduleList()

        // 클릭 리스너 설정
        setupClickListeners()

        Log.d(TAG, "onCreateView: End")
        return binding.root
    }

    private fun loadProjectDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 최신 프로젝트 가져오기
                val latestProject = withContext(Dispatchers.IO) {
                    projectRepository.getLatestProject(userId!!)
                }

                // 최신 프로젝트가 있는 경우
                latestProject?.let { project ->
                    currentProjectId = project.id // Int 타입으로 할당
                    binding.projectTitle.text = project.title
                    binding.projectDate.text = project.dateRange
                    Log.d(TAG, "loadProjectDetails: Project = $project")

                    // 프로젝트 제목 두 글자 가져오기
                    val projectInitials = withContext(Dispatchers.IO) {
                        projectRepository.getLatestProjectInitials(userId!!)
                    }
                    // 두 글자를 버튼에 출력
                    binding.projectBtn.text = projectInitials ?: "이름"
                } ?: run {
                    binding.projectTitle.text = "프로젝트 제목"
                    binding.projectDate.text = "프로젝트 날짜"
                    Log.d(TAG, "loadProjectDetails: No latest project found")
                }
            } catch (e: Exception) {
                showToast("프로젝트 세부 정보를 로드하는 중 오류가 발생했습니다: ${e.message}")
                Log.e(TAG, "loadProjectDetails: Error = ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNewWork.setOnClickListener {
            if (userId.isNullOrEmpty()) {
                showToast("사용자 아이디가 설정되지 않았습니다. 새 일정을 만들 수 없습니다.")
                Log.e(TAG, "setupClickListeners: 사용자 아이디가 설정되지 않았습니다. 새 일정을 만들 수 없습니다.")
            } else {
                val scheduleDialog = ScheduleDialogFragment.newInstance("", "", false, false, -1)
                scheduleDialog.setTargetFragment(this, 0)
                updateScheduleList()
                scheduleDialog.show(parentFragmentManager, "ScheduleDialogFragment")
                Log.d(TAG, "setupClickListeners: ScheduleDialogFragment shown")
            }
        }

        binding.plusBtn.setOnClickListener {
            if (userId.isNullOrEmpty()) {
                showToast("사용자 아이디가 설정되지 않았습니다. 새 프로젝트를 생성할 수 없습니다.")
                Log.e(TAG, "setupClickListeners: 사용자 아이디가 설정되지 않았습니다. 새 프로젝트를 생성할 수 없습니다.")
            } else {
                val listDialog = ListDialogFragment()
                listDialog.show(parentFragmentManager, "ListDialogFragment")
            }
        }

        binding.shareBtn.setOnClickListener {
            shareApp()
            Log.d(TAG, "setupClickListeners: Share button clicked")
        }
    }

    private fun shareApp() {
        val testLink = "https://github.com/conneeeection/MySecretary"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "프로젝트 일정 공유해요! 다음 링크에서 다운로드할 수 있습니다!: $testLink")
            putExtra(Intent.EXTRA_TITLE, "타이틀을 입력하세요")
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun updateScheduleList() {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "updateScheduleList: Start")
            try {
                if (userId.isNullOrEmpty()) {
                    showToast("사용자 아이디가 설정되지 않았습니다.")
                    Log.e(TAG, "updateScheduleList: 사용자 아이디가 설정되지 않았습니다.")
                    return@launch
                }

                val schedules = withContext(Dispatchers.IO) {
                    val db = scheduleDatabaseHelper.readableDatabase
                    val cursor = db.query(
                        ScheduleDatabaseHelper.TABLE_NAME,
                        null,
                        "${ScheduleDatabaseHelper.COLUMN_USER_ID} = ?",
                        arrayOf(userId!!),
                        null,
                        null,
                        null
                    )
                    val scheduleList = mutableListOf<Pair<String, String>>()
                    while (cursor.moveToNext()) {
                        val title = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_TITLE))
                        val dateRange = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_DATE_RANGE))
                        scheduleList.add(title to dateRange)
                    }
                    cursor.close()
                    scheduleList
                }

                Log.d(TAG, "updateScheduleList: ScheduleList = $schedules")

                binding.apply {
                    val adapter = ProjectAdapter(requireContext(), schedules)
                    projectContainer.adapter = adapter

                    projectContainer.setOnItemClickListener { _, _, position, _ ->
                        val (title, dateRange) = schedules[position]
                        val scheduleDialog = ScheduleDialogFragment.newInstance(
                            title, dateRange, true, true, -1
                        )
                        scheduleDialog.setTargetFragment(this@ProjectFragment, 0)
                        scheduleDialog.show(parentFragmentManager, "ScheduleDialogFragment")
                        Log.d(TAG, "updateScheduleList: ScheduleDialogFragment shown for title = $title")
                    }
                }
            } catch (e: Exception) {
                showToast("스케줄 목록을 업데이트하는 중 오류가 발생했습니다: ${e.message}")
                Log.e(TAG, "updateScheduleList: Error = ${e.message}")
            }
            Log.d(TAG, "updateScheduleList: End")
        }
    }

    override fun onScheduleCreated(id: Int, title: String, dateRange: String, isOnline: Boolean, isEdit: Boolean) {
        Log.d(TAG, "onScheduleCreated: Start, id = $id, title = $title, dateRange = $dateRange, isOnline = $isOnline, isEdit = $isEdit")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (userId.isNullOrEmpty()) {
                    showToast("사용자 아이디가 설정되지 않았습니다. 스케줄을 추가할 수 없습니다.")
                    Log.e(TAG, "onScheduleCreated: 사용자 아이디가 설정되지 않았습니다. 스케줄을 추가할 수 없습니다.")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    if (isEdit && id != -1) {
                        val updatedRows = scheduleDatabaseHelper.updateSchedule(id, title, dateRange, isOnline)
                        if (updatedRows == 0) {
                            scheduleDatabaseHelper.insertSchedule(title, dateRange, isOnline, userId!!)
                            Log.d(TAG, "onScheduleCreated: Schedule updated and new entry inserted")
                        } else {
                            Log.d(TAG, "onScheduleCreated: Schedule updated successfully")
                        }
                    } else {
                        scheduleDatabaseHelper.insertSchedule(title, dateRange, isOnline, userId!!)
                        Log.d(TAG, "onScheduleCreated: New schedule inserted")
                    }
                }

                // 스케줄 목록 갱신
                updateScheduleList()

            } catch (e: Exception) {
                showToast("스케줄을 저장하는 중 오류가 발생했습니다: ${e.message}")
                Log.e(TAG, "onScheduleCreated: Error = ${e.message}")
            }
            Log.d(TAG, "onScheduleCreated: End")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}