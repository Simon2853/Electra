package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.model.Protocolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProtocolosViewModel(
    private val repositorio: ProtocolosRepository
) : ViewModel() {

    private val _protocolos = MutableStateFlow<List<Protocolo>>(emptyList())
    val protocolos: StateFlow<List<Protocolo>> = _protocolos.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _protocolos.value = repositorio.listar().ultimosPorEquipo { it.equipo }
        }
    }
}
