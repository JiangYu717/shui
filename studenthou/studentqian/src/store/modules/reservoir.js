import axios from 'axios'

const state = {
  reservoirs: [],
  currentReservoir: null,
  loading: false,
  error: null
}

const mutations = {
  SET_RESERVOIRS(state, reservoirs) {
    state.reservoirs = reservoirs
  },
  SET_CURRENT_RESERVOIR(state, reservoir) {
    state.currentReservoir = reservoir
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  ADD_RESERVOIR(state, reservoir) {
    state.reservoirs.push(reservoir)
  },
  UPDATE_RESERVOIR(state, updatedReservoir) {
    const index = state.reservoirs.findIndex(r => r.reservoirId === updatedReservoir.reservoirId)
    if (index !== -1) {
      state.reservoirs.splice(index, 1, updatedReservoir)
    }
  },
  DELETE_RESERVOIR(state, reservoirId) {
    state.reservoirs = state.reservoirs.filter(r => r.reservoirId !== reservoirId)
  }
}

const actions = {
  async fetchReservoirs({ commit }) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get('/api/reservoirs')
      commit('SET_RESERVOIRS', response.data)
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async fetchReservoirById({ commit }, reservoirId) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`/api/reservoirs/${reservoirId}`)
      commit('SET_CURRENT_RESERVOIR', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async createReservoir({ commit }, reservoirData) {
    try {
      const response = await axios.post('/api/reservoirs', reservoirData)
      commit('ADD_RESERVOIR', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    }
  },
  
  async updateReservoir({ commit }, { reservoirId, reservoirData }) {
    try {
      const response = await axios.put(`/api/reservoirs/${reservoirId}`, reservoirData)
      commit('UPDATE_RESERVOIR', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    }
  },
  
  async deleteReservoir({ commit }, reservoirId) {
    try {
      await axios.delete(`/api/reservoirs/${reservoirId}`)
      commit('DELETE_RESERVOIR', reservoirId)
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    }
  },
  
  async searchReservoirs({ commit }, keyword) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`/api/reservoirs/search?keyword=${keyword}`)
      commit('SET_RESERVOIRS', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 