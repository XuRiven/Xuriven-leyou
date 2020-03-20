package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类id查询参数组
     *
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid) {
        List<SpecGroup> groups = this.specificationService.queryGroupsByCid(cid);
        if (CollectionUtils.isEmpty(groups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * 根据条件查询规格参数
     *
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "generic",required = false) Boolean generic,
            @RequestParam(value = "searching",required = false) Boolean searching

    ) {
        List<SpecParam> params = this.specificationService.queryParams(gid,cid,generic,searching);
        if (CollectionUtils.isEmpty(params)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }

    /**
     * 添加规格参数分组
     * @param specGroup
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> addGroups(@RequestBody SpecGroup specGroup) {
        this.specificationService.addGroups(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 修改规格参数分组
     * @param specGroup
     * @return
     */
    @RequestMapping(value = "group", method = RequestMethod.PUT)
    public ResponseEntity<Void> updateGroup(@RequestBody SpecGroup specGroup) {
        this.specificationService.updateGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id删除规格参数分组
     * @param id
     * @return
     */
    @RequestMapping(value = "group/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> updateGroup(@PathVariable Long id) {
        this.specificationService.deleteGroupById(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 添加规格参数
     * @param specParam
     * @return
     */
    @PostMapping("param")
    public ResponseEntity<Void> addParams(@RequestBody SpecParam specParam) {
        this.specificationService.addParams(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改规格参数
     * @param specParam
     * @return
     */
    @RequestMapping(value = "param",method = RequestMethod.PUT)
    public ResponseEntity<Void> updateParams(@RequestBody SpecParam specParam) {
        this.specificationService.updateParams(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id删除规格参数
     * @param id
     * @return
     */
    @RequestMapping(value = "param/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteParam(@PathVariable Long id) {
        this.specificationService.deleteParam(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
