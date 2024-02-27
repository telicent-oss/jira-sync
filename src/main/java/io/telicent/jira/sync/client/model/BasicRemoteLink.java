package io.telicent.jira.sync.client.model;

import com.atlassian.jira.rest.client.api.AddressableEntity;
import com.atlassian.jira.rest.client.api.IdentifiableEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.net.URI;

public class BasicRemoteLink implements AddressableEntity, IdentifiableEntity<Long> {
    private final URI self;
    private final Long id;

    public BasicRemoteLink(URI self, Long id) {
        this.self = self;
        this.id = id;
    }

    /**
     * @return URI of this remote link
     */
    @Override
    public URI getSelf() {
        return self;
    }

    /**
     * @return issue id
     */
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return getToStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper getToStringHelper() {
        return MoreObjects.toStringHelper(this).
                          add("self", self).
                          add("id", id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicRemoteLink) {
            BasicRemoteLink that = (BasicRemoteLink) obj;
            return Objects.equal(this.self, that.self)
                    && Objects.equal(this.id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(self, id);
    }
}
