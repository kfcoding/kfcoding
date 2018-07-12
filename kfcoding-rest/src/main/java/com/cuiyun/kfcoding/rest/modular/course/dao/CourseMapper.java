package com.cuiyun.kfcoding.rest.modular.course.dao;

import com.cuiyun.kfcoding.rest.modular.course.model.Course;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author maple
 * @since 2018-07-12
 */
public interface CourseMapper extends BaseMapper<Course> {

    /**
     *  根据Id查找课程
     */
    Course getCourseById(@Param("id") String id);
}
