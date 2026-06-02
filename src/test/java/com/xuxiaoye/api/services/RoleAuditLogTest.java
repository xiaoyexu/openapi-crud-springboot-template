package com.xuxiaoye.api.services;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import lombok.extern.log4j.Log4j2;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.services.db.RoleAuditDBService;
import com.xuxiaoye.api.services.db.RoleDBService;
import com.xuxiaoye.api.services.db.dto.entity.Role;
import com.xuxiaoye.api.services.db.dto.entity.RoleAudit;

import static com.xuxiaoye.api.interceptors.TableAuditLogInterceptor.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
public class RoleAuditLogTest {
    @Autowired
    RoleDBService roleDBService;

    @Autowired
    RoleAuditDBService roleAuditDBService;

    EasyRandom easyRandom = new EasyRandom();

    Role randomRole() {
        return easyRandom.nextObject(Role.class);
    }

    @Nested
    class Create {
        @Test
        void saveOne() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.save(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
        }

        @Test
        void saveOrUpdateOne() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
        }
    }

    @Nested
    class Update {
        @Test
        void saveOrUpdateOne() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            role.setAuthority(updatedValue);

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_UPDATE);
            assertThat(audits.get(1).getAuthority()).isEqualTo(updatedValue);
        }

        @Test
        void updateBatchByIdWithOne() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            role.setAuthority(updatedValue);

            assertDoesNotThrow(() -> roleDBService.updateBatchById(List.of(role)));

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_UPDATE);
            assertThat(audits.get(1).getAuthority()).isEqualTo(updatedValue);
        }

        @Test
        void updateBatchByIdWithTwo() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            Role role2 = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role2));

            List<RoleAudit> audits2 = roleAuditDBService.listAuditsByDataPkId(role2.getId());
            assertThat(audits2).hasSize(1);
            assertThat(audits2.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            role.setAuthority(updatedValue);

            // Update
            String updatedValue2 = easyRandom.nextObject(String.class);
            role2.setAuthority(updatedValue2);

            assertDoesNotThrow(() -> roleDBService.updateBatchById(List.of(role, role2)));

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_UPDATE);
            assertThat(audits.get(1).getAuthority()).isEqualTo(updatedValue);

            audits2 = roleAuditDBService.listAuditsByDataPkId(role2.getId());
            assertThat(audits2).hasSize(2);
            assertThat(audits2.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits2.get(1).getAction()).isEqualTo(ACTION_UPDATE);
            assertThat(audits2.get(1).getAuthority()).isEqualTo(updatedValue2);
        }

        @Test
        void updateByLambdaUpdateWrapperAndNoPKChange() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String updatedValue = easyRandom.nextObject(String.class);

            assertDoesNotThrow(() -> roleDBService.update(
                    new LambdaUpdateWrapper<Role>()
                            .eq(Role::getAuthority, role.getAuthority())
                            .set(Role::getAuthority, updatedValue))
            );

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_UPDATE);
            assertThat(audits.get(1).getAuthority()).isEqualTo(updatedValue);
        }

        @Test
        void updateByUpdateWrapperAndNoPKChange() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String updatedValue = easyRandom.nextObject(String.class);

            String column = LambdaUtils.extract(Role::getAuthority).getImplMethodName().substring(3);
            assertDoesNotThrow(() -> roleDBService.update(
                    new UpdateWrapper<Role>()
                            .eq(column, role.getAuthority())
                            .set(column, updatedValue))
            );

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_UPDATE);
            assertThat(audits.get(1).getAuthority()).isEqualTo(updatedValue);
        }

        @Test
        void updateByLambdaUpdateWrapperAndPKChange() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String newId = easyRandom.nextObject(String.class);
            String newName = easyRandom.nextObject(String.class);

            assertDoesNotThrow(() -> roleDBService.update(
                            new LambdaUpdateWrapper<Role>()
                                    .eq(Role::getId, role.getId())
                                    .set(Role::getId, newId)
                                    .set(Role::getAuthority, newName)
                    )
            );

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_DELETE);

            audits = roleAuditDBService.listAuditsByDataPkId(newId);
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(0).getId()).isEqualTo(newId);
        }

        @Test
        void updateByUpdateWrapperAndPKChange() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            // Update
            String newId = easyRandom.nextObject(String.class);
            String newName = easyRandom.nextObject(String.class);

            String column = LambdaUtils.extract(Role::getId).getImplMethodName().substring(3);
            String column2 = LambdaUtils.extract(Role::getAuthority).getImplMethodName().substring(3);

            assertDoesNotThrow(() -> roleDBService.update(
                            new UpdateWrapper<Role>()
                                    .eq(column, role.getId())
                                    .set(column, newId)
                                    .set(column2, newName)
                    )
            );

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_DELETE);
            assertThat(audits.get(1).getId()).isEqualTo(role.getId());

            audits = roleAuditDBService.listAuditsByDataPkId(newId);
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(0).getId()).isEqualTo(newId);
        }
    }

    @Nested
    class Delete {
        @Test
        void removeByIdWithOneEntity() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            assertDoesNotThrow(() -> roleDBService.removeById(role));

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_DELETE);
        }

        @Test
        void removeByIdWithIdValue() {
            Role role = randomRole();

            assertDoesNotThrow(() -> roleDBService.saveOrUpdate(role));

            List<RoleAudit> audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);

            assertDoesNotThrow(() -> roleDBService.removeById(role.getId()));

            audits = roleAuditDBService.listAuditsByDataPkId(role.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(ACTION_CREATE);
            assertThat(audits.get(1).getAction()).isEqualTo(ACTION_DELETE);
        }
    }
}