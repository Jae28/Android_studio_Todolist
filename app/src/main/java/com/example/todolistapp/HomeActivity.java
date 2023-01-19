package com.example.todolistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    //layout에서 썻던 함수들을 여기다가 옮겨서 연결시켜줌

    private DatabaseReference reference; // 데이터베이reference를 매개체 삼아서 저장하고 읽어오는 방식으로 사용됨
    private FirebaseAuth mAuth; //로그인이나 회원가입하는데 필요한 파이어베이스에 저장된 계정을 가져오기위해 FirebaseAuth 변수설정.
    private FirebaseUser mUser; //활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인하기위해서 만든 FirebaseUser 변수.
    private String onlineUserID; // 유저아이디를 받아오기위한 변수.

    private ProgressDialog loader; // 앱에서 시간이 걸리는 작업을 수행할 때 ProgressDialog 클래스를 이용하면 사용자에게 실시간 진행 상태를 알릴 수 있습니다.
    // 예를들어, 로그인 버튼을 눌렀을떄 로딩중입니다라고 뜨는 팝업창같은거라고 생각하면됨. 그 기능을 위한 변수선언.

    private String key = "";
    private String task;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Bundle은 여러가지 타입을 저장하는 Map클래스이다. Android에서는 액티비티간 데이터를 주고받을때 Bundle클래스를 사용해 다양한 데이터를
        //전송한다.액티비티(Ex. HomeActivity)를 생성하게 되면 ()안에 있는 객체를 가지고, 액티비티를 중단하게 되면 savedInstanceState메서드를
        //호출하여 데이터를 임시 저장한다. 그리고 다시 동작을 하게되면 저장된 데이터를 가지고 다시 액티비티를 생성한다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);// activity_home 레이아웃을 보여주기위해서 setContentView()메서드를 사용한다.여기서
        //R 은 res를 의미한다.즉 res폴더의 레이아웃 폴더에 activity_home이라는 이름을 가진 id 라는뜻이다.

        //초기화를먼저 시켜준다
        toolbar = findViewById(R.id.homeToolbar); //안드로이드 스튜디오에서는 view와 controller가 정확히 구분되어 있기 때문에 controller에서 view를 사용하려면 id를 통한 식별 행위가 필수적이다.
        //xml에 id 속성은 화면에 대한 View의 행동을 추가하기 위해 Java 코드를 만질 때 id 값을 xml에서 부여하고 부여된 id 값을 토대로 .java 코드에서 해당 객체를 사용할 수 있게 한다.
        //위젯 변수 = (위젯 명) findViewById(R.id.위젯_id); 의 형태를 사용하면 된다. 즉 레이아웃에서 id를 작성한것을 가져오는것을 의미한다.
        setSupportActionBar(toolbar);//setSupportActionBar 는 현재 액션바가 없으니 툴바를 액션바로 대체 하겠다는 뜻
        getSupportActionBar().setTitle("Todo List App"); // getSupportActionBar()를 통해 ActionBar옵션을 호출하고 title 을 "Todo List App"로 설정



        //리사이클러뷰(RecyclerView)는 "사용자가 관리하는 많은 수의 데이터 집합(Data Set)을 개별 아이템 단위로 구성하여 화면에 출력하는 뷰그룹(ViewGroup)이며,
        // 한 화면에 표시되기 힘든 많은 수의 데이터를 스크롤 가능한 리스트로 표시해주는 위젯"입니다.

        recyclerView = findViewById(R.id.recyclerView); // recyclerView Todolist 아이템들 보여주는 창 가져오기.


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); //레이아웃매니저는 리사이클러뷰가 아이템을 화면에 표시할 때, 아이템 뷰들이 리사이클러뷰 내부에서 배치되는 형태를 관리하는 요소입니다

        linearLayoutManager.setReverseLayout(true); //RecyclerView의 리스트 순서를 역순으로 바꿔주기위한 코드

        linearLayoutManager.setStackFromEnd(true); // RecyclerView의 리스트 순서를 역순으로 바꿔주기위한 코드 끝부터 쌓아감.

        recyclerView.setHasFixedSize(true); //아이템 항목을 추가할 때마다 RecyclerView의 크기는 변경된다. 크기가 변경되기 때문에 레이아웃을 그릴 때,
        // 크기를 측정하고 다시 그리는 것을 반복할 것이다. setHasFixedSize의 기능은 RecyclerView의 크기 변경이 일정하다는 것을 사용자의 입력으로 확인한다.
        // 항목의 높이나 너비가 변경되지 않으며, 추가 또는 제거된 모든 항목은 동일하다

        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance(); // 파이어베이스에 저장된 계정에 대한 정보를 가지고 있는 객체를 가져옵니다.
        mUser = mAuth.getCurrentUser();//활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인합니다.
        onlineUserID = mUser.getUid();// mUser에 유저아이디 가져오기.
        reference = FirebaseDatabase.getInstance().getReference().child("Task").child(onlineUserID);
        //getReference에 문자열을 넣 하부를 만드는데, child를 통해서 하부를 생성한다. 여기서는 child 가 2개쓰였으니 밑으로 2개의 child가 생성된것.

        floatingActionButton = findViewById(R.id.fab); // Home 레이아웃중에 오른쪽아래 버튼을 의미한다.

        //fab 버튼을 클릭했을때 add task해라.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });
    }

    private void addTask() {
        //AlertDialog는 사용자의 전체 화면을 가리지 않으면서 사용자의 응답이나 추가 정보를 입력하도록 하는 작은 창을 의미합니다.
        AlertDialog.Builder myDiaglog = new AlertDialog.Builder(this); // AlertDialog.Buider 객체를 생성

        LayoutInflater inflater = LayoutInflater.from(this); // Inflater를 통해 앞서 정의한 레이아웃을 View 객체로 받아와 myView가 참조하도록 합니다.
        View myView = inflater.inflate(R.layout.input_file, null);
        myDiaglog.setView(myView);


        AlertDialog dialog = myDiaglog.create();// create() 함수를 통해 AlertDialog 객체를 생성
        dialog.setCancelable(false);


        final EditText task = myView.findViewById(R.id.task); // res폴더에 id 가 task
        final EditText description = myView.findViewById(R.id.description); // res폴더에 id 가 description
        Button save = myView.findViewById(R.id.saveBtn); // res폴더에 id 가 saveBtn
        Button cancel = myView.findViewById(R.id.cancelBtn); //res폴더에 id 가 cancelBtn


        //cancel을 누르면 dialog 사라짐
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //save 누르면
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mTask = task.getText().toString().trim();
                String mDescription = description.getText().toString().trim();
                String id = reference.push().getKey();
                String data = DateFormat.getDateInstance().format(new Date());


                //텍스트가 비었으면 "Task Required"에러출력
                if (TextUtils.isEmpty(mTask)){
                    task.setError("Task Required");
                    return;
                }

                //텍스트가 비었으면 "Description Required"에러출력
                if (TextUtils.isEmpty(mDescription)){
                    description.setError("Description Required");
                }
                // 아니면 "Adding your data"출력
                else {
                    loader.setMessage("Adding your data");
                    loader.setCanceledOnTouchOutside(false); // dialog창 이외에 부분을 클릭해도 cancel이되는것 방지.
                    loader.show(); // dialog 화면에 출력

                    Model model = new Model(mTask,mDescription,id,data); // mTask,mDescription,id,data 파라미터를 가지는 model 객에
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){ // Task업로드 성공시
                                Toast.makeText(HomeActivity.this, "Task has been inserted successfully", Toast.LENGTH_SHORT).show();
                                loader.dismiss(); // 다이얼로그 사라짐
                            }else{ // Task업로드 실패시
                                String error = task.getException().toString();
                                Toast.makeText(HomeActivity.this, "Failed" + error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });

                }
                loader.dismiss();
            }

        });
        dialog.show();
    }


    //안드로이드의 Activity는 다음과 같은 생명 주기를 갖는다.
    //
    //onCreate() - Action이 생성될 때 / 화면 정의하는 용도로 많이 사용 / onCreate()가 완료되면 onStart()가 호출됨
    //onStart() - Action이 사용자에게 보여질때 / 아직 사용자와 상호작용은 불가능
    //onResume() -사용자와 상호작용 하는 단계 / Action 스택의 Top에 위치 / 주로 어플 기능이 onResume()에 설정됨
    //onPause() - Action이 잠시 멈춘 단계 / background에 Action이 위치 /onStop() 이나 onResume() 상태로 전환가능
    //onStop() - Action이 사용자에게 보이지 않는 단계 / onDestroy() 나 onRestart() 상태로 전환 가능
    //onRestart() - onStop()이던 Action이 재시작 되는 단계 / onRestart() 뒤에는 onStart()가 자동 호출된다.
    //onDestroy() - onStop()이던 상태가 완전이 제거되는 단계 / 활동이 호출하는 마지막 메소드

    //아래의 단계는 Activity의 생명주기 중에 두번째는 onStart이다.
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference, Model.class) //query를 사용해 DB값을 가져온다
                .build();
        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {


            //onBindViewHolder : 뷰홀더가 재활용될 때 실행되는 메서드
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDesc(model.getDescription());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//
                        key = getRef(holder.getAdapterPosition()).getKey();
                        task = model.getTask();
                        description = model.getDescription();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            //onCreateViewHolder : 뷰홀더를 생성(레이아웃 생성)
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_layout, parent, false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setTask(String task){
            TextView taskTextView = mView.findViewById(R.id.taskTv);
            taskTextView.setText(task);
        }
        public void setDesc(String desc){
            TextView descTextView = mView.findViewById(R.id.descriptionTv);
            descTextView.setText(desc);
        }
        public void setDate(String date){
            TextView dateTextView = mView.findViewById(R.id.dateTv);
        }
    }


    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText mTask = view.findViewById(R.id.mTextTask);
        EditText mDescription = view.findViewById(R.id.mTextDescription);

        mTask.setText(task);
        mTask.setSelection(task.length());

        mDescription.setText(description);
        mDescription.setSelection(description.length());

        Button delButton = view.findViewById(R.id.btnDelete);
        Button updateButton = view.findViewById(R.id.btnUpdate);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = mTask.getText().toString().trim();
                description = mDescription.getText().toString().trim();

                String date = DateFormat.getDateInstance().format(new Date());
//DB에 넣어야함

                Model model = new Model(task,description,key,date);
                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Date has been updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Updated failed"+err, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                dialog.dismiss();
            }
        });
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed to deleted task" + err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });



                dialog.dismiss();
            }
        });

        dialog.show();
    }



    //Activity의 onCreateOptionsMenu() 함수를 오버라이딩 합니다. 해당 함수는 액티비티(Activity)가 시작될 때 호출되는 함수로
    // 액티비티 Life Cycle 내에서 단 한 번만 호출되기 때문에 이 안에서 MenuItem 생성과 초기화를 하면 됩니다. 해당 함수에서는
    // Menu Inflater를 통하여 XML Menu 리소스에 정의된 내용을 파싱 하여 Menu 객체를 생성하고 추가를 하게 됩니다.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate는 부풀게하다 라는뜻이다. 안드로이드에서 inflate 의 정의는 xml에 표기된 레이아웃들을 메모리에 객체화시키는 행동이다.
        //쉽게 말해서, xml코드들은 객체화해서 코드에서 사용하기 위함이다. 기본적으로, 안드로이드에서는 화면(Activity)을 하나만들면, 소스코드하나와
        //화면을 구성하는 xml하나 이렇게 2개가 생성되는데, 이때 setContentView()함수가 바로 자동으로 만들어진 xml을 객체화시키는 inflate 동작이다.
        //그렇기때문에, 우리는 setContentView()함수 밑에서 xml에 배치했던 UI요소들을 맘껏 끌어와 쓸수있다.
        //그렇다면 만약, 다른화면을 구성하는 xml을 불러오고싶다면 어떨까? 이런경우는
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    // Activity의 onOptionsItemSelected() 함수는 옵션 메뉴(Option Menu)에서 특정 Menu Item을 선택하였을 때 호출되는 함수입니다.
    // 매개변수로 선택 된 MenuItem의 객체가 넘어옵니다.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut(); //계정 로그아웃
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class); // 로그아웃을 하면 Home 에서 로그인창으로 넘어간다

                //Intent.FLAG_ACTIVITY_NEW_TASK =새로운 태스크 생성
                //Intent.FLAG_ACTIVITY_CLEAR_TASK = 실행 액티비티 외 모두 제거
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

        }
        return super.onOptionsItemSelected(item);
    }
}


//https://m.blog.naver.com/kkrdiamond77/221305647401
//https://dongkyprogramming.tistory.com/4
//https://mainia.tistory.com/2031
//https://alka-loid.tistory.com/40
//https://wonit.tistory.com/161
//https://recipes4dev.tistory.com/154
//https://woovictory.github.io/2020/06/24/Android-RecyclerView-Attr/
//https://bbaktaeho-95.tistory.com/73