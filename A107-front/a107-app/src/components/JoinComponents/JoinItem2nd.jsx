import '../../Styles/temp_border_style.css'
import '../../Styles/JoinItem2Style.css'

import JoinItemName from './JoinItemName'
import JoinItemPhoneNumber from './JoinItemPhoneNumber'
import JoinItemGender from './JoinItemGender'
import { useState } from 'react'
import NextPageButton from './NextPageButton'
import JoinPassword from './JoinPassword'

const JoinItem2nd = (props) => {
  const [hasName, setHasName] = useState(false)
  const [hasGender, setHasGender] = useState(false)
  const [hasPhoneNumber, setHasPhoneNumber] = useState(true)
  const [hasPW, setHasPW] = useState(false)
  const wholeHas = [
    [hasName, setHasName],
    [hasGender, setHasGender],
    [hasPhoneNumber, setHasPhoneNumber],
    [hasPW, setHasPW],
  ]

  const setWholeHas = (index, value) => {
    console.log(index, value)
    wholeHas[index][1](value)
  }
  return (
    <div className="display">
      <div className="whole-block">
        <h2>Let's Build Your Own Rocket (1/3)</h2>
        {`hasName : ${hasName}`}
        <div className="whole-items">
          <div className="left-items">
            <div
              className={
                'each-items name-item ' + (hasName ? 'border-glow' : '')
              }
            >
              <JoinItemName setHas={setWholeHas} />
            </div>
            <div
              className={'each-items pw-item ' + (hasPW ? 'border-glow' : '')}
            >
              <JoinPassword setHas={setWholeHas} />
            </div>
          </div>
          <div className="right-items">
            <div
              className={
                'each-items phonenumber-item ' +
                (hasPhoneNumber ? 'border-glow' : '')
              }
            >
              <JoinItemPhoneNumber setHas={setWholeHas} />
            </div>
            <div
              className={
                'each-items gender-item ' + (hasGender ? 'border-glow' : '')
              }
            >
              <JoinItemGender setHas={setWholeHas} />
            </div>
          </div>
        </div>
        <div className="next-button">
          <NextPageButton
            hasName={hasName}
            hasGender={hasGender}
            hasPhoneNumber={hasPhoneNumber}
            hasPW={hasPW}
            setNext={props.setNext}
          />
        </div>
      </div>
    </div>
  )
}

export default JoinItem2nd