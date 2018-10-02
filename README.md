# [HeapCon](https://heapcon.io/)

One of the great things about cloud deployments is the easy scaling. You might run your application on multiple machines, changing
the number of machines up and down perhaps through a PAAS engine marshalling an IAAS provider.

This is all fine for processing, but what about data stores?

If these are hosted on a single server, this quickly becomes a bottleneck that limits the ability for the processing to deliver on
the scaling promise.

In this talk we’ll look at Hazelcast in-memory data grid (IMDG), and how this can distribute and re-distribute data records evenly
across multiple hosts. Now data can scale as easily as processing. Problem solved!

Well, not quite. There are a few irritating details to be aware of, so we’ll end this talk with a demo of Hazelcast data-grid running
on some providers, to see how the problems manifest and how they are solved.

