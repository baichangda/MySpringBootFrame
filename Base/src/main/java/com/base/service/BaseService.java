package com.base.service;

import com.base.annotation.ReferCollection;
import com.base.annotation.ReferredCollection;
import com.base.condition.BaseCondition;
import com.base.util.BeanUtil;
import com.base.util.ConditionUtil;
import com.base.util.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Administrator on 2017/4/11.
 */
@SuppressWarnings("unchecked")
public class BaseService<T,K extends Serializable> {
    @PersistenceContext
    public EntityManager em;

    @Autowired
    public com.base.repository.BaseRepository<T,K> repository;

    @Transactional
    public void deleteAll(){
        repository.deleteAll();
    }

    public Page<T> findAll(List<BaseCondition> conditionList, Pageable pageable){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(conditionList);
        return repository.findAll(specification,pageable);
    }

    public Page<T> findAll(BaseCondition condition, Pageable pageable){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(condition);
        return repository.findAll(specification,pageable);
    }

    public List<T> findAll(List<BaseCondition> conditionList){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(conditionList);
        return repository.findAll(specification);
    }

    public List<T> findAll(BaseCondition condition){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(condition);
        return repository.findAll(specification);
    }

    public List<T> findAll(List<BaseCondition> conditionList, Sort sort){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(conditionList);
        return repository.findAll(specification,sort);
    }

    public List<T> findAll(BaseCondition condition, Sort sort){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(condition);
        return repository.findAll(specification,sort);
    }

    public T findOne(K k){
        return repository.findOne(k);
    }

    public T findOne(List<BaseCondition> conditionList){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(conditionList);
        return repository.findOne(specification);
    }

    public T findOne(BaseCondition condition){
        Specification<T> specification= ConditionUtil.getSpecificationByCondition(condition);
        return repository.findOne(specification);
    }

    public List<T> findAll(){
        return repository.findAll();
    }

    public List<T> findAll(Sort sort){
        return repository.findAll(sort);
    }

    public List<T> findAll(Iterable<K> iterable){
        return repository.findAll(iterable);
    }

    public List<T> findAll(K[] kArr){
        return repository.findAll(Arrays.asList(kArr));
    }

    @Transactional
    public T save(T t){
        return repository.save(t);
    }

    @Transactional
    public List<T> save(Iterable<T> iterable){
        return repository.save(iterable);
    }

    @Transactional
    public void delete(K[] ids){
        for(int i=0;i<=ids.length-1;i++){
            repository.delete(ids[i]);
        }
    }

    @Transactional
    public void delete(T t){
        repository.delete(t);
    }

    @Transactional
    public void delete(Iterable<T> iterable){
        repository.delete(iterable);
    }


    /**
     * 逻辑为
     * 先查询出数据库对应记录
     * 然后将参数对象非空值注入到数据库对象中
     * 保存对象
     *
     * 只支持主键为 BeanUtil.BASE_DATA_TYPE 对应的数据类型
     *
     * 会改成传入参数的值
     *
     * @param t
     */
    @Transactional
    public T saveIngoreNull(T t){
        T returnVal;
        Object val= BeanUtil.getPKValByJPAAnnotation(t);
        if(val==null){
            returnVal=save(t);
        }else{
            T dbt=findOne((K)val);
            if(dbt==null){
                throw new RuntimeException("未找到对应Id的记录，无法进行更新!");
            }
            BeanUtil.autoInversionForBaseAttrForNull(dbt,t);
            returnVal=save(t);
        }
        return returnVal;
    }

    /**
     * 逻辑为
     * 先查询出数据库对应记录
     * 然后将参数对象非空值注入到数据库对象中
     * 保存数据库对象
     *
     * 会改变传入参数的值
     *
     * @param iterable
     */
    @Transactional
    public List<T> saveIngoreNull(Iterable<T> iterable){
        List<T> returnVal=new ArrayList<T>();
        Iterator<T> it= iterable.iterator();
        while(it.hasNext()){
            T t=it.next();
            returnVal.add(saveIngoreNull(t));
        }
        return returnVal;
    }


    /**
     * 优于普通删除方法
     * @param conditionList
     * @return 删除的记录条数
     */
    @Transactional
    public int delete(List<BaseCondition> conditionList){
        Class<T> clazz= (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Specification specification= ConditionUtil.getSpecificationByCondition(conditionList);
        CriteriaBuilder criteriaBuilder= em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery= criteriaBuilder.createQuery(clazz);
        CriteriaDelete criteriaDelete= criteriaBuilder.createCriteriaDelete(clazz);
        Predicate predicate= specification.toPredicate(criteriaDelete.from(clazz),criteriaQuery,criteriaBuilder);
        criteriaDelete.where(predicate);
        return em.createQuery(criteriaDelete).executeUpdate();
    }

    /**
     * 优于普通删除方法
     * @param condition
     * @return 删除的记录条数
     */
    @Transactional
    public int delete(BaseCondition condition){
        Class<T> clazz= (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Specification specification= ConditionUtil.getSpecificationByCondition(condition);
        CriteriaBuilder criteriaBuilder= em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery= criteriaBuilder.createQuery(clazz);
        CriteriaDelete criteriaDelete= criteriaBuilder.createCriteriaDelete(clazz);
        Predicate predicate= specification.toPredicate(criteriaDelete.from(clazz),criteriaQuery,criteriaBuilder);
        criteriaDelete.where(predicate);
        return em.createQuery(criteriaDelete).executeUpdate();
    }

    /**
     * 批量保存、有aop
     * @param list
     */
    @Transactional
    public void saveBatch(List<T> list){
        List<T> insertList=new ArrayList<>();
        List<T> updateList=new ArrayList<>();
        list.forEach(e->{
            Object obj=BeanUtil.getPKValByJPAAnnotation(e);
            if(obj==null){
                insertList.add(e);
            }else{
                updateList.add(e);
            }
        });
        for (int i = 0; i < insertList.size(); i++) {
            em.persist(insertList.get(i));
            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
        for (int i = 0; i < updateList.size(); i++) {
            em.merge(updateList.get(i));
            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    /**
     * 批量插入,no aop
     * @param list
     */
    @Transactional
    public void insertBatch(List<T> list){
        for (int i = 0; i < list.size(); i++) {
            em.persist(list.get(i));
            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    /**
     * 批量更新,no aop
     * @param list
     */
    @Transactional
    public void updateBatch(List<T> list){
        for (int i = 0; i < list.size(); i++) {
            em.merge(list.get(i));
            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }


    /**
     * 优于普通更新方法
     * @param conditionList
     * @param attrMap 更新的字段和值的map
     * @return 更新的记录条数
     */
    @Transactional
    public int update(List<BaseCondition> conditionList, Map<String,Object> attrMap){
        if(attrMap==null||attrMap.size()==0){
            return 0;
        }
        Class<T> clazz= (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Specification specification= ConditionUtil.getSpecificationByCondition(conditionList);
        CriteriaBuilder criteriaBuilder= em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery= criteriaBuilder.createQuery(clazz);
        CriteriaUpdate criteriaUpdate= criteriaBuilder.createCriteriaUpdate(clazz);
        Predicate predicate= specification.toPredicate(criteriaUpdate.from(clazz),criteriaQuery,criteriaBuilder);
        criteriaUpdate.where(predicate);
        Iterator<Map.Entry<String,Object>> it= attrMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Object> entry= it.next();
            criteriaUpdate.set(entry.getKey(),entry.getValue());
        }
        return em.createQuery(criteriaUpdate).executeUpdate();
    }

    /**
     * 优于普通更新方法
     * @param condition
     * @param attrMap 更新的字段和值的map
     * @return 更新的记录条数
     */
    @Transactional
    public int update(BaseCondition condition, Map<String,Object> attrMap){
        if(attrMap==null||attrMap.size()==0){
            return 0;
        }
        Class<T> clazz= (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Specification specification= ConditionUtil.getSpecificationByCondition(condition);
        CriteriaBuilder criteriaBuilder= em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery= criteriaBuilder.createQuery(clazz);
        CriteriaUpdate criteriaUpdate= criteriaBuilder.createCriteriaUpdate(clazz);
        Predicate predicate= specification.toPredicate(criteriaUpdate.from(clazz),criteriaQuery,criteriaBuilder);
        criteriaUpdate.where(predicate);
        Iterator<Map.Entry<String,Object>> it= attrMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Object> entry= it.next();
            criteriaUpdate.set(entry.getKey(),entry.getValue());
        }
        return em.createQuery(criteriaUpdate).executeUpdate();
    }

    /**
     * 根据native sql查询
     * @param sql
     * @return
     */
    public Query queryByNativeSql(String sql,Iterable<Object> params){
        Query query= em.createNativeQuery(sql);
        if(params!=null) {
            Iterator it = params.iterator();
            int index = 1;
            while (it.hasNext()) {
                query.setParameter(index, it.next());
                index++;
            }
        }
        return query;
    }

    /**
     * 保存实体进行重复的关联关系的验证
     * 只能验证manytomany关系且必须严格按照如下格式:
     *   @ReferCollection
     *   @ManyToMany
     *   @JoinTable(
     *   name = "t_adas_sim",
     *   joinColumns =@JoinColumn(name = "adas_id",referencedColumnName = "id"),
     *   inverseJoinColumns = @JoinColumn(name = "sim_id",referencedColumnName = "id")
     *   )
     *   private Set<?> set=new HashSet<?>();
     *
     *   备注:
     *   1、set属性需要有 get 方法
     *   2、所有实体属性主键必须是id且为long类型
     * @param t
     */
    public void saveWithNoRepeatRefer(T t){
        //1、获取当前对象类型
        Class clazz= t.getClass();
        //2、获取当前类型所有带ManyToMany注解的字段集合
        List<Field> fieldList= BeanUtil.getFieldList(clazz,ManyToMany.class);
        //3、遍历字段集合
        fieldList.forEach(field -> {
            //3.1、获取父关系的注解msg key值
            Annotation ccAnnotation= field.getAnnotation(ReferCollection.class);
            if(ccAnnotation==null){
                ccAnnotation=field.getDeclaredAnnotation(ReferCollection.class);
            }
            if(ccAnnotation==null){
                return;
            }
            String msgKey=((ReferCollection)ccAnnotation).saveHasRepeatMessageKey();

            //3.2、获取字段的JoinTable注解
            Annotation annotation= field.getAnnotation(JoinTable.class);
            if(annotation==null){
                annotation=field.getDeclaredAnnotation(JoinTable.class);
            }
            if(annotation==null){
                return;
            }
            //3.3、获取JoinTable注解里面的各种值
            JoinColumn[] joinColumns= ((JoinTable) annotation).joinColumns();
            JoinColumn[] inverseJoinColumns= ((JoinTable) annotation).inverseJoinColumns();
            String tableName = ((JoinTable) annotation).name();
            if(joinColumns==null||joinColumns.length==0
                    ||inverseJoinColumns==null||inverseJoinColumns.length==0
                    ||tableName==null){
                return;
            }
            String columnName=joinColumns[0].name();
            String inverseColumnName=inverseJoinColumns[0].name();
            if(columnName==null||inverseColumnName==null){
                return;
            }
            //3.4、获取当前字段值、并验证是否属于Set类型
            String fieldName=field.getName();
            Object collectionObj= BeanUtil.getFieldVal(t,fieldName);
            if(collectionObj==null||!Collection.class.isAssignableFrom(collectionObj.getClass())){
                return;
            }

            //3.6、获取当前对象主键值
            Object id=BeanUtil.getPKValByJPAAnnotation(t);
            //3.7、遍历set集合
            ((Collection)collectionObj).forEach(e->{
                //3.7.1、获取关联关系对象主键值
                Object inverseId= BeanUtil.getPKValByJPAAnnotation(e);
                //3.7.2、构造关联关系表查询sql
                StringBuffer sb=new StringBuffer();
                sb.append("select ");
                sb.append(columnName);
                sb.append(" from ");
                sb.append(tableName);
                sb.append(" where ");
                sb.append(inverseColumnName);
                sb.append("=?");
                Query query= em.createNativeQuery(sb.toString());
                query.setParameter(1,inverseId);

                //3.7.3、如果当前操作是新增
                if(id==null){
                    if(query.getMaxResults()>0){
                        throw new RuntimeException(I18nUtil.getMessage(msgKey));
                    }
                }else{
                    //3.7.4、如果是编辑
                    List result= query.getResultList();
                    //3.7.5、如果关联关系结果大于1条、则说明数据库绑定记录存在错误
                    if(result.size()>1){
                        throw new RuntimeException(I18nUtil.getMessage(msgKey));
                    }else if(result.size()==1){
                        //3.7.6、如果是1条、则验证绑定关系另一端是不是当前对象
                        Object dbId=((BigInteger)result.get(0)).longValue();
                        if(dbId!=id){
                            throw new RuntimeException(I18nUtil.getMessage(msgKey));
                        }
                    }
                }
            });
        });

        //4、如果所有的验证均没有抛出异常、则说明没有重复绑定关系;则进行保存
        save(t);

    }

    /**
     * @param id
     */
    @Transactional
    public void deleteWithNoReferred(K id){
        //1、先查出当前id的对象
        T t=findOne(id);
        //2、进行删除
        deleteWithNoReferred(t);
    }

    /**
     * 删除实体之前进行子关系的验证,如果存在子集关系,则抛出异常
     * 只适用于 ReferredCollection && (ManyToMany || OneToMany || OneToOne) 关系验证
     * @param t
     */
    @Transactional
    public void deleteWithNoReferred(T t){
        //1、获取当前对象类型
        Class clazz= t.getClass();
        //2、获取当前类型所有带ParentCollection注解的字段集合
        List<Field> fieldList= BeanUtil.getFieldList(clazz,ReferredCollection.class);

        fieldList.forEach(field->{
            Annotation annotation= field.getAnnotation(ReferredCollection.class);
            if(annotation==null){
                annotation=field.getDeclaredAnnotation(ReferredCollection.class);
            }
            if(annotation==null){
                return;
            }

            //2.1、获取当前字段值
            Object objVal=BeanUtil.getFieldVal(t,field.getName());
            //2.2、如果为空则直接跳过当前字段验证
            if(objVal==null){
                return;
            }

            //2.3、获取父关系的注解msg key值
            String msgKey=((ReferredCollection)annotation).deleteHasRelationMessageKey();
            //2.4、如果是属于集合类、则验证集合类元素是否为空
            if(Collection.class.isAssignableFrom(objVal.getClass())){
                if(((Collection)objVal).size()>0){
                    throw new RuntimeException(I18nUtil.getMessage(msgKey));
                }
            }else{
                //2.5、如果不为空也不为集合类,则说明存在对象引用关联,直接抛出异常
                throw new RuntimeException(I18nUtil.getMessage(msgKey));
            }

        });

        //3、如果所有验证都没有抛出异常,则删除
        delete(t);
    }

    /**
     *
     * @param ids
     */
    @Transactional
    public void deleteWithNoReferred(K[] ids){
        for(int i=0;i<=ids.length-1;i++){
            deleteWithNoReferred(ids[i]);
        }
    }

    /**
     * 字段唯一性验证
     * @param fieldName 属性名称
     * @param val 属性值
     * @return
     */
    @Transactional
    public boolean isUnique(String fieldName,String val){
        boolean flag = true;
        List<T> resultList = repository.findAll(new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                List<Expression<Boolean>> expressions = predicate.getExpressions();
                expressions.add(criteriaBuilder.equal(root.get(fieldName),val));
                return predicate;
            }
        });
        if (resultList!=null&&resultList.size()>0){
            flag = false;
        }
        return flag;
    }
}
