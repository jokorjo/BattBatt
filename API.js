import axios from 'axios';

const API_URL = 'http://localhost:8080/api/'; // Backendin URL

export const getBatteries = async () => {
  try {
    const response = await axios.get(`${API_URL}batteries`);
    return response;
  } catch (error) {
    console.error('Virhe akkujen haussa:', error);
  }
};

export const getStorage = async () => {
  try {
    const response = await axios.get(`${API_URL}storage`);
    return response;
  } catch (error) {
    console.error('Virhe varastojen haussa:', error);
  }
};

export const addBatteryToStorage = async (batteryId) => {
  try {
    const response = await axios.post(`${API_URL}addToStorage`, { batteryId });
    console.log('Akku lisätty varastoon:', response.data);
  } catch (error) {
    console.error('Virhe akku varastoon lisäyksessä:', error);
  }
};
