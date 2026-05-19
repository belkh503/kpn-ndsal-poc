package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LockObjectRepository extends Neo4jRepository<LockObjectEntity, UUID> {

    /**
     * To get only those locked object which are register with given @domain in LockRequestEntiry and lockObjectEntity name present in given resources
     *
     * @param domain
     *         as BCPE, EDIN
     * @param resources
     *         as ENE, EVA...
     * @return List of LockObjectEntity
     */
    @Query("""
            MATCH(lockrequest:LockRequestEntity {domain: $domain})-[HAS_LOCKED_ENTITY]->(lockobject:LockObjectEntity)
            where lockobject.name IN  $resources
            RETURN lockobject
            """)
    List<LockObjectEntity> findByDomainAndLockObjectEntitiesNameIn(@Param("domain") String domain,
            @Param("resources") List<String> resources);

}
