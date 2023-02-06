import React from 'react'
import { NavLink } from 'react-router-dom'

const Home = () => {
  console.log('처음 홈 화면')
  return (
    <div>
      <div style={{ height: '100px' }}></div>홈
      <div>
        <NavLink to="/login">로그인</NavLink>
      </div>
      <div>
        <NavLink to="/join">조인</NavLink>
      </div>
    </div>
  )
}

export default Home