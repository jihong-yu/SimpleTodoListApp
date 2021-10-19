package com.jimdac_todolist.todolistpractice2

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import com.jimdac_todolist.todolistpractice2.databinding.ActivityMainBinding
import com.jimdac_todolist.todolistpractice2.databinding.TodoItemListBinding

class MainActivity : AppCompatActivity() {
    //레이아웃 내부의 데이터에 접근하기 위해 뷰바인딩을 사용
    private lateinit var binding: ActivityMainBinding
    private val myViewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //커스텀 툴바를 해당 액티비티에 설정
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 툴바에 왼쪽 상단버튼 만들기
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24) //왼쪽상단버튼 아이콘지정

        //네비게이션 뷰 작동을 위해 툴바와 드로어블레이아웃 연결을 위한 토글 생성
        val actionBarDrawerToggle = ActionBarDrawerToggle(this,binding.mainDrawerRoot,
            binding.mainToolbar,R.string.drawer_open,R.string.drawer_close)
        //드로어 리스너에 토글 설정
        binding.mainDrawerRoot.addDrawerListener(actionBarDrawerToggle)
        //네비게이션 아이템 클릭시 실행할 리스너 등록
        binding.mainNaviView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.test1 -> {
                    finish()
                    true
                }
                else -> true
            }
        }

        //아답터 생성
       val adapter = TodoAdapter(emptyList(), onClickDeleteIcon = {
            myViewModel.deleteFunc(it)
        }, onClickEditText = {
            myViewModel.toggleFunc(it)
        })
        //리사이클러뷰에 아답터 장착
        binding.mainRecyclerview.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(this@MainActivity)
            //리사이클러뷰를 리니어 레이아웃으로 설정
        }

        //추가 버튼 리스너 등록
        binding.addButton.setOnClickListener {
            myViewModel.addFunc(Todo(binding.editText.text.toString()))
            binding.editText.setText("")
        }

        //뷰모델 내부의 라이브데이터가 변경될 때마다 호출되는 옵저버 구현(it에는 변경되는 라이브데이터 내부의 함수가 전달된다.)
        myViewModel.liveTodoData.observe(this@MainActivity, {
            (binding.mainRecyclerview.adapter as TodoAdapter).setData(it)
        })
    }

    //뒤로가기 버튼 눌렀을 때 네비게이션 닫기
    override fun onBackPressed() {
        if(binding.mainDrawerRoot.isDrawerOpen(GravityCompat.START)){
            binding.mainDrawerRoot.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    
    // 오른쪽 상단의 메뉴에 등록할 메뉴레이아웃 등록
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        return true
    }
    //툴바에 위치한 메뉴 선택시 실행할 리스너 등록
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            R.id.home -> {
                binding.mainDrawerRoot.isDrawerOpen(GravityCompat.START)
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

    }
    //로그아웃처리
    fun logout() {
        Firebase.auth.signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

}

//text,완료여부,id 값을 담을 data 클래스 선언
data class Todo(val text: String, var isDone: Boolean = false, val documentId: Int = 0)

//리사이클러뷰에 담을 아답터 선언(data를 파이어베이스에서 넘겨주는 DocumentSnapshot 으로 설정한다.)
class TodoAdapter(//삭제아이콘과 텍스트를 클릭했을 때 실행되는 함수를 인자로 넘겨준다.
    private var dataSet: List<DocumentSnapshot>, val onClickDeleteIcon: (todo: DocumentSnapshot) -> Unit,
    val onClickEditText: (todo: DocumentSnapshot) -> Unit
) :
    RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    //binding값을 뷰홀더의 프로퍼티를 todo_item_list xml의 바인딩 함수로 지정하여 바로 해당 레이아웃의 뷰들에 접근할 수 있게끔 설정
    class ViewHolder(val binding: TodoItemListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.todo_item_list, viewGroup, false)
        return ViewHolder(TodoItemListBinding.bind(view))
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding.itemTextView.text = dataSet[position].getString("text")
        viewHolder.binding.itemDeleteIcon.setOnClickListener {
            onClickDeleteIcon.invoke(dataSet[position])
        }
        viewHolder.binding.itemTextView.setOnClickListener {
            onClickEditText.invoke(dataSet[position])
        }
        //만약 데이터내부의 isDone값이 True라면 글자 스타일 변경
        if (dataSet[position].getBoolean("isDone") ?: false) {
            viewHolder.binding.itemTextView.apply {
                this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                this.setTypeface(null, Typeface.ITALIC)
            }
            //만약 데이터 내부의 siDone값이 False라면 글자 스타일 원본 복구
        } else {
            viewHolder.binding.itemTextView.apply {
                this.paintFlags = 0
                this.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    override fun getItemCount() = dataSet.size
    
    //아덥테 내부 데이터에 새로운 데이터를 넣어주는 함수
    fun setData(newData: List<DocumentSnapshot>) {
        dataSet = newData
        notifyDataSetChanged()
    }
}

//화면을 회전해도 데이터를 계속해서 저장해주는 뷰모델 선언 
class MyViewModel : ViewModel() {
    
    //데이터를 실시간으로 관리하기 위해 라이브데이터 선언
    val liveTodoData: MutableLiveData<List<DocumentSnapshot>> by lazy {
        MutableLiveData<List<DocumentSnapshot>>()
    }
    //id값 
    private var i: Int = 0
    //파이어스토어 데이터베이스 선언
    private val db = Firebase.firestore
    //현재 유저 객체 생성
    private val user = Firebase.auth.currentUser

    init {
        //만약 현재유저가 null값이 아니라면 실시간으로 파이어베이스의 데이터가 변경될때 마다 데이터를 계속해서 받아온다.
        if (user != null) {
            db.collection(user.uid).orderBy("isDone") //isDone 값을 기준으로 정렬(false면 뒤쪽으로 정렬이된다.)
                .addSnapshotListener { value, e ->
                    if (e != null) { 
                        Log.w("TAG", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (value != null) {
                        liveTodoData.value = value.documents //라이브데이터에 바로 파이어베이스에서 넘겨주는 데이터를 대입
                        Log.d("TAG", "value : "+value)
                        for (document in value) { 
                            i = document.getField("documentId") ?: 0 //i값을 계속해서 유지시키기 위해 데이터에 저장되어있는 i값을 받아와서 i에 저장
                        }

                    }

                }
        }
    }
    //리사이클러뷰에 데이터를 추가하는 함수(문서 id를 뷰모델 내부에 저장되어 있는 i값으로 선언)
    fun addFunc(todo: Todo) {
        if (user != null) {
            i += 1
            val todoMap = hashMapOf("text" to todo.text, "isDone" to todo.isDone, "documentId" to i)
            //isDone이 Done으로 저장되는 문제가 있어 isDone으로 명시 후 hashmap형태로 데이터를 추가해준다.
            db.collection(user.uid).document(i.toString())
                .set(todoMap)
                .addOnSuccessListener { documentReference ->
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        }

    }
    
    //파이어베이스의 데이터를 삭제
    fun deleteFunc(todo: DocumentSnapshot) {
        if (user != null) {
            db.collection(user.uid).document(todo.id).delete()
                .addOnSuccessListener {
                }.addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
        }
    }
    //파이어베이스의 데이터를 수정(텍스트를 클릭할 경우 취소선 및 기울임 꼴로 바꾸어줌 다시 클릭할 경우 복원)
    fun toggleFunc(todo: DocumentSnapshot) {
        if (user != null) {
            db.collection(user.uid).document(todo.id)
                .update("isDone", !((todo.getBoolean("isDone")) ?: false)).addOnSuccessListener {
                    //(텍스트를 클릭할 경우 isDone값을 반대값으로 바꾸어 준다.)
                }.addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        }

    }
}