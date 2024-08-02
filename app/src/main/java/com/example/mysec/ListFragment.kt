package com.example.mysec

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.ListFragment

class ListFragment : Fragment() {
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

        // 생성된 뷰 반환
        return view
    }

    // 팝업 창 표시 메서드
    private fun showDialog() {
        val dialogFragment = ListDialogFragment()
        dialogFragment.setTargetFragment(this, 0)
        dialogFragment.show(parentFragmentManager, "ListDialogFragment")
    }
}