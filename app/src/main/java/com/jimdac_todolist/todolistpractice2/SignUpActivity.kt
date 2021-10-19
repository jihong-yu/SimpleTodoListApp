package com.jimdac_todolist.todolistpractice2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jimdac_todolist.todolistpractice2.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySignUpBinding
    private lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        Log.d("TAG", "auth: " + auth)
        //뒤로가기 버튼 리스너 등록
        binding.signupBackbutton.setOnClickListener {
            onBackPressed() //뒤로가기 등록
        }

        binding.signupButton.setOnClickListener {

            val email = binding.inputSignupEmail.text.toString()
            val password = binding.inputSignupPassword1.text.toString()
            val password2 = binding.inputSignupPassword2.text.toString()
            //패스워드1,2가 같을 경우와 다를 경우 분기해서 처리
            if (password == password2){
                //패스워드1,2가 같다면 각 텍스트 공백처리
                if (email != "" && password != "" && password2 != ""){

                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(baseContext, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@SignUpActivity,MainActivity::class.java))
                                    finish()
                                } else {
                                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                                    Toast.makeText(baseContext, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                                }
                            }

                } else {
                    Toast.makeText(this@SignUpActivity, "아이디와 비밀번호는 공백일 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@SignUpActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.inputSignupEmail.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(s != null && s.length <= 5){
                    binding.signupEmailCheck.text = "이메일 또는 아이디는 6자 이상이어야 합니다."
                } else{
                    binding.signupEmailCheck.text = ""
                }
            }
        })

        binding.inputSignupPassword1.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(s != null && s.length <= 5){
                    binding.signupPassword1Check.text = "비밀번호는 6자 이상이어야 합니다."
                } else{
                    binding.signupPassword1Check.text = ""
                }
            }
        })

        binding.inputSignupPassword2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //..
            }

            override fun afterTextChanged(s: Editable?) {
                if(s != null && s.length <= 5){
                    binding.signupPassword2Check.text = "비밀번호는 6자 이상이어야 합니다."
                } else{
                    binding.signupPassword2Check.text = ""
                }
            }
        })
    }
}