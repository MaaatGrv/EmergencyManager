// src/App.js

import React from 'react';
import Header from './components/Header';
import HomePage from './components/HomePage';
import Map from './components/Map';

function App() {
  return (
    <div className="App">
      <Header />
      <HomePage />
      <Map />
    </div>
  );
}

export default App;
