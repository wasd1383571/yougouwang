package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;
    /*
    * 添加商户
    * */
    @Override
    public void add(Seller seller) {

        //密码加密
        seller.setPassword(new BCryptPasswordEncoder().encode(seller.getPassword()));
        //设置商家状态
        seller.setStatus("0");
        //设置创建时间
        seller.setCreateTime(new Date());

        sellerDao.insertSelective(seller);
    }
    /*
    * 商家审核 分页查询
    * */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {

        //分页插件
        PageHelper.startPage(page,rows);

        //创建对象
        SellerQuery sellerQuery = new SellerQuery();
        //创建内部对象
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        //查询未审核状态
        criteria.andStatusEqualTo(seller.getStatus());
        //判断
        if (null != seller.getName() && !"".equals(seller.getName().trim())){

            criteria.andNameLike("%"+seller.getName()+"%");
        }

        if (null != seller.getNickName() && !"".equals(seller.getNickName().trim())){

            criteria.andNickNameLike("%"+seller.getNickName()+"%");
        }

         //查询结果集
        Page<Seller> sellerPage = (Page<Seller>) sellerDao.selectByExample(sellerQuery);

          return new PageResult(sellerPage.getTotal(),sellerPage.getResult());
    }
    /*
    * 详情  数据回显
    * */
    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }
    /*
    * 商家审核  状态
    * */
    @Override
    public void updateStatus(String sellerId, String status) {

        Seller seller = new Seller();

        seller.setSellerId(sellerId);

        seller.setStatus(status);

        sellerDao.updateByPrimaryKeySelective(seller);

    }
}
