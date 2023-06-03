// src/components/Header.js

import React from 'react';
import styled from 'styled-components';

const HeaderContainer = styled.header`
  background-color: #282c34;
  min-height: 10vh;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  font-size: calc(10px + 2vmin);
  color: white;
  padding: 0 20px;
`;

const Title = styled.h1`
  font-weight: bold;
`;

const Header = () => (
  <HeaderContainer>
    <Title>Firefighter Futuristic App</Title>
  </HeaderContainer>
);

export default Header;
