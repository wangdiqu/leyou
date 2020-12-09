package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;
import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO> (){
        {
            AddressDTO addressDTO=new AddressDTO();
            addressDTO.setId(1L);
            addressDTO.setName("虎哥");
            addressDTO.setAddress("航头镇航头路18号传智播客 3号楼");
            addressDTO.setCity("上海");
            addressDTO.setDistrict("浦东新区");
            addressDTO.setIsDefault(true);
            addressDTO.setPhone("15800000000");
            addressDTO.setState("上海");
            addressDTO.setZipCode("210000");
            add(addressDTO);

            AddressDTO addressDTO1=new AddressDTO();
            addressDTO1.setId(2L);
            addressDTO1.setName("张三");
            addressDTO1.setAddress("朝阳路 3号楼");
            addressDTO1.setCity("北京");
            addressDTO1.setDistrict("朝阳区");
            addressDTO1.setIsDefault(false);
            addressDTO1.setPhone("13600000000");
            addressDTO1.setState("北京");
            addressDTO1.setZipCode("100000");
            add(addressDTO1);
        }
    };
    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList){
            System.out.println(addressDTO.getId() == id);
            if (addressDTO.getId() == id) {
                return addressDTO;
            }
        }
        return null;
    }
}
