import React, { useEffect, useState } from 'react';
import axios from 'axios';

const BatteryList = () => {
  const [batteries, setBatteries] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8080/api/batteries')
      .then(response => setBatteries(response.data))
      .catch(error => console.error(error));
  }, []);

  return (
    <div>
      <h2>Batteries</h2>
      <ul>
        {batteries.map(battery => (
          <li key={battery.id}>{battery.name} - {battery.type}</li>
        ))}
      </ul>
    </div>
  );
};

export default BatteryList;
