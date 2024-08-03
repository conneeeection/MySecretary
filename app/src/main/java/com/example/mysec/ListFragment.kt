package com.example.mysec

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class ListFragment : Fragment() {
    private lateinit var dbHelper: DBHelper
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ListFragment.ARG_USER_ID)
            Log.d(ContentValues.TAG, "User ID: $userId") // ID 확인 로그 추가
        }
        dbHelper = DBHelper(requireContext())
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        @JvmStatic
        fun newInstance(userId: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }

    // 프래그먼트 뷰 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_list 레이아웃을 인플레이트하여 뷰 생성
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        // 프로젝트 생성 버튼을 누르면 팝업창을 표시
        val createProjectButton = view.findViewById<Button>(R.id.createProject_button)
        createProjectButton.setOnClickListener {
            // 팝업 창 표시 메서드 호출
            showDialog()
        }

        // 사용자 아이디 가져오기
        userId = userId ?: dbHelper.getUserId()

        // DB에서 사용자 정보를 가져와서 표시
        userId?.let {
            val userInfo = dbHelper.getUserId()
            Log.d("ProjectFragment", "User Id: $userId") // 로그 추가
        }

        // 생성된 뷰 반환
        return view
    }

    // 팝업 창 표시 메서드
    private fun showDialog() {
        // 사용자가 입력한 ID를 얻어서 ProjectFragment에 전달
        val dialogFragment = ListDialogFragment().apply {
            // 새로운 인스턴스 생성과 함께 사용자 ID 전달
            arguments = Bundle().apply {
                putString(ARG_USER_ID, userId)
            }
        }
        dialogFragment.show(parentFragmentManager, "ListDialogFragment")
    }
}
