package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhang.reggie.common.R;
import com.zhang.reggie.entity.Employee;
import com.zhang.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * @RequestBody 主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)；
         * GET方式无请求体，所以使用@RequestBody接收数据时，前端不能使用GET方式提交数据，而是用POST方式进行提交。
         * request用于把登录成功用户信息 存session
         */
        //1.前端提交代码做MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.通过前端用户名查询数据库
            //创建mybatisPlus函数参数所需对象
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
            //前端username赋值给queryWrapper的对象
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3.判断是否有用户名
        if(emp == null){
            return R.error("用户名不存在！");
        }

        //4.密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误！");
        }

        //5、查看状态
        if(emp.getStatus() == 0){
            return R.error("状态已禁用");
        }

        //员工 id 放入 session
            //注意时emp的id不是employee！！！
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //1.清除session中的值
        HttpSession session = request.getSession();
        session.removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工：{}",employee.toString());
        //设置初始密码,进行加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获取当前用户ID
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        //mybatisPlus中的方法，
        //用户可能创建同名账户，需要全局异常捕获
        employeeService.save(employee);

        return R.success("新增员工成功！");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.like(StringUtils.hasText(name),Employee::getName,name);

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据ID修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){

        //employee.setUpdateTime(LocalDateTime.now());
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateUser(empId); //JS只能处理16位，Long是19位

        employeeService.updateById(employee);
        return R.success("员工信息修改成功！");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("查询员工信息！");
        Employee employee = employeeService.getById(id);
        if (employee!=null){
            return R.success(employee);
        }
        return R.error("查询失败！");
    }
}
