import axios from 'axios'

export function getWeather() {
  return axios.get('/WeatherForecast')
}
