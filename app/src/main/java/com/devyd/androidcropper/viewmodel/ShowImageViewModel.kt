package com.devyd.androidcropper.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.devyd.androidcropper.state.ShowImageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ShowImageViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    // MutableStateFlow의 setter에 접근하기 위해 따로 생성.
    private val _state = MutableStateFlow(ShowImageState())
    // 외부에 읽기전용으로 노출하기 위해 생성.
    val state : StateFlow<ShowImageState> = _state

    fun initState(state : ShowImageState) {
        _state.update { state }
    }

    fun undo() {
        _state.update {
            if(isUndoPossible()) {
                it.redoStack.push(it.undoStack.pop())
            }
            it.copy(recompositionTrigger = it.recompositionTrigger + 1)
        }
    }

    fun redo() {
        _state.update {
            if(isRedoPossible()){
                it.undoStack.push(it.redoStack.pop())
            }
            it.copy(recompositionTrigger = it.recompositionTrigger + 1)
        }
    }

    fun isUndoPossible() = state.value.undoStack.size > 1

    fun isRedoPossible() = state.value.redoStack.isNotEmpty()

    fun getCurrentBitmap() : Bitmap {
        val bitmapStack = state.value.undoStack
        if(bitmapStack.isEmpty()){
            // 빈 비트맵을 리턴? 또는 에러처리.
        }
        return bitmapStack.peek()
    }
}