package iie.dcs.test;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by xsyin on 17-12-6.
 */

public class ContactsManager {
    private ContentResolver contentResolver;

    private static final String TAG = "ContactsManager";

    public ContactsManager(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public Contact searchContactByNumber(String phoneNumber){
        Contact contact = new Contact();
        contact.setPhoneNumber(phoneNumber);
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER+"= '"+phoneNumber+"'";
        Cursor  cursor = null;
        try {
            cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,selection,null,null);
            if (cursor != null && cursor.moveToFirst()) {
               do {
                   String id = cursor.getString(0);
                   contact.setId(id);
                   String name = cursor.getString(1);
                   contact.setName(name);
                   Log.d(TAG, "searchContactByNumber: ");
//                   Cursor noteCursor = null;
//                   try {
//                       noteCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
//                               new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Note.NOTE},
//                               ContactsContract.Data.CONTACT_ID+"=?"+" AND "
//                       + ContactsContract.Data.MIMETYPE+"=?",
//                               new String[]{id, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE},null);
//                       if (noteCursor != null && noteCursor.moveToFirst()) {
//                           String note = noteCursor.getString(1);
//                           Log.d(TAG, "searchContactByNumber: "+note);
//                       }
//                   } catch (Exception e) {
//                       e.printStackTrace();
//                   } finally {
//                       if (noteCursor != null) {
//                           noteCursor.close();
//                       }
//                   }

               }while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return contact;
    }

    public boolean updateContact(Contact contact){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.RAW_CONTACT_ID+"=? AND "
                        + ContactsContract.Data.MIMETYPE+"=?",
                        new String[]{contact.getId(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE,contact.getRemarks())
                .build());


        try {

            contentResolver.applyBatch(ContactsContract.AUTHORITY,ops);

        } catch (RemoteException e) {
            e.printStackTrace();
            return  false;
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return  false;
        }
        return  true;
    }


}

