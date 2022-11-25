package com.example.messagingbasicexample

import android.app.Application

class MessagingApp : Application() {
    val viewModel = AppViewModel.ViewModelFactory(this).create(AppViewModel::class.java)
}