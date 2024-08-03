package com.example.mysec

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnSignup: Button
    private var db: DBHelper? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DBHelper(this)
        sessionManager = SessionManager(this)

        btnLogin = findViewById(R.id.login_button)
        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextPassword)
        btnSignup = findViewById(R.id.signup_button)

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener {
            val user = editTextId.text.toString()
            val pass = editTextPassword.text.toString()

            if (user.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val isValidUser = db?.checkUserpass(user, pass) ?: false
                    // 로그인 성공 시 Toast를 띄우고 MainActivity로 전환
                    if (isValidUser) {
                        // 세션 생성
                        sessionManager.createLoginSession(user)

                        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(MainActivity.ARG_USER_ID, user) // 사용자 ID 전달
                        startActivity(intent)
                        finish() // 현재 액티비티 종료
                    } else {
                        Toast.makeText(this, "아이디와 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // 예외 발생 시 로그와 Toast 메시지 출력
                    e.printStackTrace()
                    Toast.makeText(this, "로그인 처리 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 회원가입 버튼 클릭 시
        btnSignup.setOnClickListener {
            val signupIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupIntent)
        }
    }
}
