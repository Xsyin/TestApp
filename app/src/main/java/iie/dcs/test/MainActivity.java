package iie.dcs.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import iie.dcs.crypto.Crypto;
import iie.dcs.utils.StringUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Crypto mCrypto=Crypto.getInstance();

    private Button mPubKeyBtn=null, mSignDataBtn=null;
    private TextView mMsgText=null;
    Contact contact = new Contact();
    private Button UpdateBtn = null;
    private EditText ePhone = null, pk = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMsgText=(TextView)findViewById(R.id.msg_text);
        mPubKeyBtn=(Button)findViewById(R.id.get_pub_key_btn);
        mSignDataBtn=(Button)findViewById(R.id.sign_data_btn);
        Button SendPKtoBtn = (Button)findViewById(R.id.send_pk_to);
        UpdateBtn = (Button)findViewById(R.id.search);
        ePhone = (EditText)findViewById(R.id.phone_number);
        pk = (EditText)findViewById(R.id.public_key);
        UpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_CONTACTS},1);
                }else{
                    updateContact();
                }

            }
        });

        Log.d(TAG, "onCreate: ");
        SendPKtoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });

       //连接安全核心服务
        if(!mCrypto.ConnectSecureCore(MainActivity.this)){
            mMsgText.setText("尚未安装安全核心APP,无法连接服务");
            return;
        }

        mPubKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mCrypto.isReady()){
                    mMsgText.setText("安全核心APP尚未连接");
                    return;
                }
                byte[] pubKey=mCrypto.getPublicKey();
                if(pubKey==null){
                    mMsgText.setText("从安全核心获取公钥失败");
                    return;
                }
                String s= StringUtils.bytesToHexString(pubKey);
                mMsgText.setText(s);
            }
        });

        mSignDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mCrypto.isReady()){
                    mMsgText.setText("安全核心APP尚未连接");
                    return;
                }

                byte[] data=new byte[]{1,2,3}; //待签名的数据
                byte[] sig=mCrypto.hashAndSignData(data);
                if(sig==null){
                    mMsgText.setText("安全核心签名失败");
                    return;
                }
                long rs=mCrypto.hashAndVerifyData(data,sig);
                if(rs!=0){
                    mMsgText.setText("安全核心验签失败");
                    return;
                }

                String s=StringUtils.bytesToHexString(sig);
                mMsgText.setText(s);
            }
        });



    }


    public void updateContact(){
        String phoneNumber = ePhone.getText().toString();
        String publicKey = pk.getText().toString();
        Log.d(TAG, "onClick: "+publicKey);
        ContactsManager contactsManager = new ContactsManager(MainActivity.this.getContentResolver());
        contact = contactsManager.searchContactByNumber(phoneNumber);
        contact.setRemarks(publicKey);
        if (contactsManager.updateContact(contact))
        {
            Toast.makeText(MainActivity.this, "写入成功", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this, "写入失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateContact();
                }else{
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCrypto!=null)
            mCrypto.DisconnectService();
    }


}
