// src/components/HomePage.js

import React from 'react';
import styled from 'styled-components';

const HomePageContainer = styled.div`
  text-align: center;
  background-color: #282c34;
  min-height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: calc(10px + 2vmin);
  color: white;
`;

const HomePage = () => (
  <HomePageContainer>
    <p>Welcome to the Firefighter Futuristic App!</p>
  </HomePageContainer>
);

export default HomePage;
