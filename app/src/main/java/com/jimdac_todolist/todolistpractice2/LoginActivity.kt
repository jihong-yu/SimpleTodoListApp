package com.jimdac_todolist.todolistpractice2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jimdac_todolist.todolistpractice2.databinding.ActivityLoginBinding
import com.jimdac_todolist.todolistpractice2.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        //만약 현재 로그인유저가 존재한다면 바로 MainActivity로 이동
        if (auth.currentUser != null){
            finish()
            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        auth = Firebase.auth

        //로그인 버튼 리스너 등록
        binding.loginButton.setOnClickListener {
            //이메일과 패스워드 데이터를 그대로 email과 password에 대입
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            //eamil 이나 password 공백 처리
            if (email == "" || password == ""){
                Toast.makeText(this@LoginActivity, "아이디와 비밀번호는 공백일 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else{ //공백아 아니라면 로그인 처리
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success")
                            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "아이디와 비밀번호를 확인해주세요.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

            }

        }
        //회원가입 버튼 리스너 등록
        binding.signupText.setOnClickListener {
            startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
        }
        
        //입력받을 때마다 길이가 6자리 이하일 경우 표시
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(s != null && s.length <= 5){
                    binding.loginEmailCheck.text = "이메일 혹은 아이디는 6자 이상이어야 합니다."
                } else{
                    binding.loginEmailCheck.text = ""
                }
            }
        })
        //입력받을 때마다 길이가 6자리 이하일 경우 표시
        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(s != null && s.length <= 5){
                    binding.loginPasswordCheck.text = "비밀번호는 6자 이상이어야 합니다."
                } else{
                    binding.loginPasswordCheck.text = ""
                }
            }
        })
    }
}