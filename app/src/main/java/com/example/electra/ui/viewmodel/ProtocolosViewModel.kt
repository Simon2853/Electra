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

    /**
     * La pantalla lo llama en cada ON_RESUME para reflejar protocolos recién creados.
     * Expone solo los últimos [LIMITE_RECIENTES_POR_EQUIPO] de cada equipo; los demás siguen en la base.
     */
    fun cargar() {
        viewModelScope.launch {
            _protocolos.value = repositorio.listar().ultimosPorEquipo { it.equipo }
        }
    }
}
