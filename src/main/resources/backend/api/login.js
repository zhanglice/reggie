function loginApi(data) {
  return $axios({  //通过Ajax向后端服务发送请求
    'url': '/employee/login',
    'method': 'post',
    data  //携带的数据
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}
