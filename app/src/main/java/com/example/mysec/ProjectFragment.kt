package com.example.mysec

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mysec.databinding.ProjectDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectFragment : Fragment() {

    private var _binding: ProjectDialogBinding? = null
    private val binding get() = _binding

    // 프로젝트 저장을 위한 Repository 인스턴스
    private lateinit var projectRepository: ProjectRepository
    private lateinit var scheduleDatabaseHelper: ScheduleDatabaseHelper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // View 바인딩 초기화
        _binding = ProjectDialogBinding.inflate(inflater, container, false)
        // Repository 초기화
        projectRepository = ProjectRepository(requireContext())
        scheduleDatabaseHelper = ScheduleDatabaseHelper(requireContext())

        // 전달된 project_initials 값을 가져옴
        val projectInitials = arguments?.getString("project_initials", "")

        // project_btn의 텍스트를 설정
        binding?.projectBtn?.text = projectInitials

        // CoroutineScope를 사용하여 비동기로 데이터베이스에서 데이터 가져오기
        viewLifecycleOwner.lifecycleScope.launch {
            val project = withContext(Dispatchers.IO) {
                val dbHelper = ProjectDatabaseHelper(requireContext())
                val db = dbHelper.readableDatabase
                // 데이터베이스 쿼리 실행하여 최신 프로젝트 가져오기
                val cursor = db.query("projects", null, null, null, null, null, "id DESC", "1")
                if (cursor.moveToFirst()) {
                    val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                    val dateRange = cursor.getString(cursor.getColumnIndexOrThrow("date_range"))
                    cursor.close()
                    Pair(title, dateRange)
                } else {
                    cursor.close()
                    Pair("프로젝트 제목", "프로젝트 기간")
                }
            }
            val projectInitials = withContext(Dispatchers.IO) {
                projectRepository.getLatestProjectInitials()
            }
            binding?.projectBtn?.text = projectInitials

            // 가져온 데이터 바인딩 전에 _binding이 null이 아닌지 확인
            _binding?.let {
                binding?.projectBtn?.text = projectInitials
                // 가져온 데이터 바인딩
                if (isAdded) { // 프래그먼트가 아직 추가된 상태인지 확인
                    binding?.projectTitle?.setText(project.first)
                    binding?.projectDate?.setText(project.second)
                }
            }
        }

        binding?.btnNewWork?.setOnClickListener {
            val scheduleDialog = ScheduleDialogFragment().apply {
                setTargetFragment(this@ProjectFragment, 0)
            }
            scheduleDialog.show(parentFragmentManager, "ScheduleDialogFragment")
        }

        updateScheduleList()

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // View 바인딩 해제
        _binding = null
    }

    fun updateScheduleList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val schedules = withContext(Dispatchers.IO) {
                val db = scheduleDatabaseHelper.readableDatabase
                val cursor = db.query(
                    ScheduleDatabaseHelper.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                val scheduleList = mutableListOf<String>()
                val scheduleMap = mutableMapOf<String, Pair<String, Boolean>>()
                while (cursor.moveToNext()) {
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_TITLE))
                    val dateRange = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_DATE_RANGE))
                    val isOnline = cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_IS_ONLINE)) == 1
                    scheduleList.add(title)
                    scheduleMap[title] = Pair(dateRange, isOnline)
                }
                cursor.close()
                Pair(scheduleList, scheduleMap)
            }

            _binding?.let {
                val (scheduleList, scheduleMap) = schedules
                val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, scheduleList)
                binding?.projectContainer?.adapter = adapter

                binding?.projectContainer?.setOnItemClickListener { _, _, position, _ ->
                    val title = scheduleList[position]
                    val (dateRange, isOnline) = scheduleMap[title] ?: Pair("", false)
                    val scheduleDialog = ScheduleDialogFragment().apply {
                        arguments = Bundle().apply {
                            putString("title", title)
                            putString("dateRange", dateRange)
                            putBoolean("isOnline", isOnline)
                            putBoolean("isEdit", true)
                            // 추가: id 전달
                            putInt("id", scheduleDatabaseHelper.getScheduleId(title))
                        }
                        setTargetFragment(this@ProjectFragment, 0)
                    }
                    scheduleDialog.show(parentFragmentManager, "ScheduleDialogFragment")
                }
            }
        }
    }


    fun saveSchedule(id: Int, title: String, dateRange: String, isOnline: Boolean, isEdit: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (isEdit && id != -1) {
                    // 기존 일정 업데이트
                    val updatedRows = scheduleDatabaseHelper.updateSchedule(id, title, dateRange, isOnline)
                    if (updatedRows == 0) {
                        // 업데이트된 행이 없으면 새 일정 추가
                        scheduleDatabaseHelper.insertSchedule(title, dateRange, isOnline)
                    }
                } else {
                    // 새로운 일정 추가
                    scheduleDatabaseHelper.insertSchedule(title, dateRange, isOnline)
                }
                // 리스트 갱신
                updateScheduleList()
            }
        }
    }

}