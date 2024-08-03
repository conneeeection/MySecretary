package com.example.mysec

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

private const val ARG_USER_ID = "user_id"
private const val TAG = "MypageFragment"

class MypageFragment : Fragment() {

    private var userId: String? = null
    private lateinit var nameTextView: TextView
    private lateinit var idTextView: TextView
    private lateinit var editButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    private lateinit var quitButton: Button
    private lateinit var nameEditText: EditText
    private var dbHelper: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            Log.d(TAG, "User ID: $userId") // ID 확인 로그 추가
        }
        dbHelper = DBHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        // 초기화 시점에서 findViewById 호출
        nameTextView = view.findViewById(R.id.name_text_view)
        idTextView = view.findViewById(R.id.id_text_view)
        editButton = view.findViewById(R.id.edit_button)
        saveButton = view.findViewById(R.id.save_button)
        logoutButton = view.findViewById(R.id.logout_button)
        quitButton = view.findViewById(R.id.quit_button)
        nameEditText = view.findViewById(R.id.name_edit_text)

        // 초기 상태 설정
        nameEditText.visibility = View.GONE
        saveButton.visibility = View.INVISIBLE

        // DB에서 사용자 정보를 가져와서 표시
        userId?.let {
            val userInfo = dbHelper?.getUserInfo(it)
            Log.d(TAG, "User Info: $userInfo") // 로그 추가
            if (userInfo != null) {
                nameTextView.text = "${userInfo.name}"
                idTextView.text = "${userInfo.id}"
            } else {
                // userInfo가 null인 경우 처리
                nameTextView.text = "정보 없음"
                idTextView.text = "정보 없음"
            }
        }

        // 수정 버튼 클릭 리스너 설정
        editButton.setOnClickListener {
            Log.d(TAG, "Edit Button Clicked") // 클릭 로그 추가
            nameTextView.visibility = View.GONE
            nameEditText.visibility = View.VISIBLE
            nameEditText.setText(nameTextView.text.toString())
            saveButton.visibility = View.VISIBLE
        }

        // 저장 버튼 클릭 리스너 설정
        saveButton.setOnClickListener {
            Log.d(TAG, "Save Button Clicked") // 클릭 로그 추가
            val newName = nameEditText.text.toString()
            userId?.let {
                val updated = dbHelper?.updateUserName(it, newName) ?: false
                if (updated) {
                    nameTextView.text = "${newName}"
                    nameEditText.visibility = View.GONE
                    nameTextView.visibility = View.VISIBLE
                    saveButton.visibility = View.INVISIBLE
                } else {
                    Log.e(TAG, "Failed to update user name")
                }
            }
        }

        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener {
            Log.d(TAG, "Logout Button Clicked") // 클릭 로그 추가
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // 회원 탈퇴 버튼 클릭 리스너 설정
        quitButton.setOnClickListener {
            Log.d(TAG, "Quit Button Clicked") // 클릭 로그 추가
            userId?.let {
                val deleted = dbHelper?.deleteUser(it) ?: false
                if (deleted) {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Log.e(TAG, "Failed to delete user")
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
            MypageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}
