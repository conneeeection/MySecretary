package com.example.mysec

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    private var DB: DBHelper? = null
    private var CheckId = false
    private lateinit var nameField: EditText
    private lateinit var idField: EditText
    private lateinit var btnCheckIdReg: Button
    private lateinit var passwordField: EditText
    private lateinit var passwordCheckField: EditText
    private lateinit var signupBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // DB 헬퍼 초기화
        DB = DBHelper(this)

        nameField = findViewById(R.id.name_field)
        idField = findViewById(R.id.id_field)
        btnCheckIdReg = findViewById(R.id.btnCheckId_Reg)
        passwordField = findViewById(R.id.password_field)
        passwordCheckField = findViewById(R.id.password_check_field)
        signupBtn = findViewById(R.id.signup_button)

        // 아이디 중복확인
        btnCheckIdReg.setOnClickListener {
            val user = idField.text.toString()
            val idPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{6,15}$"    // 영문/숫자 6~15자

            if (user.isEmpty()) {
                Toast.makeText(
                    this@SignupActivity,
                    "아이디를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (Pattern.matches(idPattern, user)) {
                    val checkUsername = DB!!.checkUser(user)
                    if (!checkUsername) {
                        CheckId = true
                        Toast.makeText(this@SignupActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SignupActivity, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignupActivity, "아이디 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 완료 버튼 클릭 시
        signupBtn.setOnClickListener {
            val user = idField.text.toString()
            val pass = passwordField.text.toString()
            val repass = passwordCheckField.text.toString()
            val name = nameField.text.toString()
            val pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{8,15}$"    // 영문/숫자 8~15자

            // 사용자 입력이 비었을 때
            if (user.isEmpty() || pass.isEmpty() || repass.isEmpty() || name.isEmpty()) {
                Toast.makeText(this@SignupActivity, "회원정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 아이디 중복 확인이 됐을 때
                if (CheckId) {
                    // 비밀번호 형식이 맞을 때
                    if (Pattern.matches(pwPattern, pass)) {
                        // 비밀번호 재확인 성공
                        if (pass == repass) {
                            val insert = DB!!.insertData(user, pass, name)
                            // 가입 성공 시 Toast를 띄우고 메인 화면으로 전환
                            if (insert) {
                                Toast.makeText(this@SignupActivity, "가입되었습니다.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                // 가입 실패 시
                                Toast.makeText(this@SignupActivity, "가입 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 비밀번호 재확인 실패
                            Toast.makeText(this@SignupActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 비밀번호 형식이 맞지 않을 때
                        Toast.makeText(this@SignupActivity, "비밀번호 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 아이디 중복확인이 되지 않았을 때
                    Toast.makeText(this@SignupActivity, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
