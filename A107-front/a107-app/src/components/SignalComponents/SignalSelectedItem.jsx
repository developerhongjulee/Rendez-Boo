const SignalListItem = ({ chat }) => {
  const { message, created_at, senderSeq } = chat
  return (
    <div>
      {/* <h1>SignalSelectedItem</h1> */}
      <p>{senderSeq}</p>
      <div>
        <p>{message}</p>
        <p>{created_at}</p>
      </div>
    </div>
  )
}

export default SignalListItem