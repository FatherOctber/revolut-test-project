package com.fatheroctober.dbadapter

import java.io.FileInputStream

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.google.inject.Singleton

@Singleton
class FirebaseAdapter(dbUrl: String) extends Persistence[String, String] {

  def init(serviceAccount: FileInputStream): FirebaseAdapter = {
    def options = new FirebaseOptions.Builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccount))
      .setDatabaseUrl(dbUrl)
      .build()

    FirebaseApp.initializeApp(options)
    this
  }

  override def <<(key: String, data: String): Unit = ??? // todo implementation

  override def >>(key: String): String = ??? // todo implementation

  override def update(compare: (String) => String, key: String): Unit = ??? // todo implementation
}
