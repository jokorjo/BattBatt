import React, { useState } from "react";
import BatteryList from "./BatteryList";
import Scanner from "./Scanner";

function App() {
  const [batteries, setBatteries] = useState([]);
  const [scannedCode, setScannedCode] = useState("");

  const handleScan = (code) => {
    setScannedCode(code);
    alert(`Skannattu koodi: ${code}`);
  };

  return (
    <div className="App">
      <h1>Battery Management System</h1>
      <Scanner onDetected={handleScan} />
      <BatteryList batteries={batteries} />
    </div>
  );
}

export default App;
